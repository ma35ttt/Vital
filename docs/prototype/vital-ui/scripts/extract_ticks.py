"""
Derive the dial tick data directly from the reference image.

The big dial is unwrapped into polar coordinates (angle x radius). Every tick
track then becomes a row of vertical dashes that can be measured exactly:
angle, inner radius, outer radius, thickness and colour.

Output: src/tick-data.js  (plain data, consumed by src/dial.js)
Run:    python3 -I scripts/extract_ticks.py
Needs:  opencv-python, numpy, scipy
"""
import json, math, sys
import cv2
import numpy as np
from scipy.signal import find_peaks

REF = "design/reference-full.png"
OUT = "src/tick-data.js"

# --- frame calibration (reference px -> CSS px) -----------------------------
SCREEN_X, SCREEN_Y = 99, 80          # top-left of the phone screen inside the screenshot
SCALE = 660 / 393                    # reference px per CSS px
CX, CY = 587.02, 916.21         # dial centre in reference px (subpixel edge fit, residual 0.19 px)

H = 2880                             # polar rows -> 0.125 degrees per row
im = cv2.imread(REF)
pol = cv2.warpPolar(im, (480, H), (CX, CY), 480, cv2.WARP_POLAR_LINEAR | cv2.INTER_CUBIC)
rgb = pol[:, :, ::-1].astype(float)
lum = rgb.max(2)
BG = 7.0

# track name: (radius band to search, grid step in degrees, visible angle window)
TRACKS = {
    "rim":  dict(band=(166, 196), step=11.25, window=(0, 360), min_sep=8),
    "bars": dict(band=(212, 320), step=7.2, window=(40, 175)),
    "mid":  dict(band=(372, 432), step=7.2, window=(56, 304)),
    "fine": dict(band=(438, 464), step=3.6, window=(60, 300)),
}


# UI that sits on top of the dial in the reference. Ticks passing behind it cannot be measured.
OCCLUDERS = [
    ("circle", 246, 575, 132),            # knob + dark ring
    ("rect", 270, 598, 480, 800),         # activity connector + icons
    ("rect", 118, 888, 355, 950),         # chevron + "4.98 km"
    ("rect", 495, 552, 680, 616),         # 09:24 pill
    ("rect", 498, 1018, 678, 1080),       # 128bpm pill
    ("rect", 110, 1255, 330, 1475),       # FAB + L/M/H bracket
]


def occluded(x, y):
    for o in OCCLUDERS:
        if o[0] == "circle" and math.hypot(x - o[1], y - o[2]) < o[3]:
            return True
        if o[0] == "rect" and o[1] <= x <= o[3] and o[2] <= y <= o[4]:
            return True
    return False


def deg(row):
    return row * 360.0 / H


def row_of(angle):
    return int(round((angle % 360) / 360.0 * H)) % H


def fit_grid(angles, step):
    """Phase of a uniform grid that best matches the detected peaks."""
    ph = 2 * math.pi * np.asarray(angles) / step
    return (math.atan2(np.sin(ph).sum(), np.cos(ph).sum()) / (2 * math.pi) * step) % step


def measure(angle, a, b):
    """Radial extent, thickness and colour of the tick at `angle` inside band (a, b)."""
    for rr in range(a, b + 1, 3):
        if occluded(CX + rr * math.cos(math.radians(angle)), CY + rr * math.sin(math.radians(angle))):
            return None
    r0 = row_of(angle)
    rows = [(r0 + k) % H for k in range(-2, 3)]
    prof = np.max([lum[r, a:b] for r in rows], axis=0)
    peak = prof.max()
    if peak < BG + (3 if a >= 438 else 10):
        return None
    thr = BG + 0.35 * (peak - BG)
    idx = np.where(prof > thr)[0]
    if len(idx) == 0:
        return None
    r_in, r_out = a + idx.min(), a + idx.max() + 1
    mid = int((r_in + r_out) / 2)
    # thickness: FWHM across angle at the middle of the tick
    col = np.array([lum[(r0 + k) % H, mid] for k in range(-24, 25)])
    half = BG + 0.5 * (col.max() - BG)
    width_rows = (col > half).sum()
    width_px = math.radians(width_rows * 360.0 / H) * mid
    # colour: brightest pixel along the tick, at the centre row
    best = max(range(r_in, r_out), key=lambda r: max(lum[q, r] for q in rows))
    c = np.max([rgb[q, best] for q in rows], axis=0).astype(int)
    return dict(r_in=int(r_in), r_out=int(r_out), width_px=float(width_px), rgb=[int(c[0]), int(c[1]), int(c[2])])


out = {}
for name, cfg in TRACKS.items():
    a, b = cfg["band"]
    lo, hi = cfg["window"]
    prof = np.convolve(lum[:, a:b].max(1), np.ones(5) / 5, "same")
    sep = cfg.get("min_sep", 0.9)
    pk, _ = find_peaks(prof, height=BG + (6 if name == "rim" else 14), distance=int(sep * H / 360), prominence=4 if name == "rim" else 8)
    angs = [deg(p) for p in pk if lo <= deg(p) <= hi]
    step = cfg["step"]
    if step is None:
        d = np.diff(sorted(angs))
        step = float(np.median(d[d > 0.5]))
    phase = fit_grid(angs, step)
    ticks = []
    k0 = math.ceil((lo - phase) / step)
    k1 = math.floor((hi - phase) / step)
    for k in range(k0, k1 + 1):
        ang = phase + k * step
        rm = (a + b) / 2
        px = CX + rm * math.cos(math.radians(ang))
        py = CY + rm * math.sin(math.radians(ang))
        if not (SCREEN_X + 8 < px < SCREEN_X + 660 - 8 and SCREEN_Y + 4 < py < SCREEN_Y + 1436 - 4):
            continue                                   # lies on the phone bezel / off screen
        m = measure(ang, a, b)
        ticks.append(dict(angle=round(ang, 3), **(m or {})))
    out[name] = dict(step=round(step, 4), phase=round(phase, 3), ticks=ticks)
    ok = sum(1 for t in ticks if "r_in" in t)
    print(f"{name:5s} step={step:.3f} phase={phase:.2f} ticks={len(ticks)} measured={ok}", file=sys.stderr)

# rim ticks are barely visible (near-black on black): use the median measurement for all of them
_rim = [t for t in out["rim"]["ticks"] if "r_in" in t]
if _rim:
    med = lambda k: int(np.median([t[k] for t in _rim]))
    col = [int(np.median([t["rgb"][i] for t in _rim])) for i in range(3)]
    for t in out["rim"]["ticks"]:
        t.update(r_in=med("r_in"), r_out=med("r_out"), width_px=2.4, rgb=col)

# --- convert to CSS px and write ---------------------------------------------
def css(v):
    return round(v / SCALE, 2)

result = {
    "center": [css(CX - SCREEN_X), css(CY - SCREEN_Y)],
    "tracks": {},
}
for name, tr in out.items():
    ticks = []
    for t in tr["ticks"]:
        if "r_in" not in t:
            ticks.append({"a": t["angle"]})        # occluded in the reference: filled in by interpolation
            continue
        ticks.append({
            "a": t["angle"],
            "r0": css(t["r_in"]),
            "r1": css(t["r_out"]),
            "w": round(max(t["width_px"] / SCALE, 0.6), 2),
            "c": "#%02x%02x%02x" % tuple(t["rgb"]),
        })
    result["tracks"][name] = {"step": tr["step"], "ticks": ticks}

with open(OUT, "w") as f:
    f.write("// GENERATED by scripts/extract_ticks.py from design/reference-full.png - do not hand edit.\n")
    f.write("export const TICKS = " + json.dumps(result, indent=1) + ";\n")
print("wrote", OUT, file=sys.stderr)
