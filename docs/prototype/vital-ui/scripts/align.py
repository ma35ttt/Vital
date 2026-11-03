"""
Measure where each piece of text/ink sits in the reference vs the render and print the delta.
    python3 -I scripts/align.py
Deltas are in reference px (1 css px = 1.679 ref px). Positive dx = render is too far right,
positive dy = too low, positive dw/dh = render is too wide/tall.
Run `node scripts/diff.mjs` first so out/render.png is fresh.
"""
import numpy as np
from PIL import Image

ref = np.array(Image.open("design/reference.png").convert("RGB")).astype(int)
cur = np.array(Image.open("out/render.png").convert("RGB")).astype(int)

dark = lambda a: a.max(2) < 70          # black text on orange / gray
light = lambda a: a.min(2) > 150        # white text on black
gray = lambda a: (a.min(2) > 70) & (a.max(2) - a.min(2) < 30)   # grey label on black
orange_on_dark = lambda a: (a[:, :, 0] > 150) & (a[:, :, 1] < 120)

# name: (region x0,y0,x1,y1 in reference-screen px, predicate)
ITEMS = {
    "clock 9:41":      ((30, 40, 120, 75),     dark),
    "SUMMARY":         ((200, 110, 460, 160),  dark),
    "SUN":             ((30, 120, 140, 158),   dark),
    "APR.9":           ((545, 120, 630, 158),  dark),
    "MOVE":            ((15, 195, 130, 240),   dark),
    "306cal":          ((15, 240, 360, 335),   dark),
    "SHOW MORE text":  ((450, 270, 622, 310),  dark),
    "09:24 text":      ((420, 485, 560, 525),  dark),
    "4.98 km":         ((60, 810, 260, 860),   light),
    "STEPS":           ((335, 755, 440, 790),  gray),
    "11.624":          ((335, 800, 650, 880),  light),
    "128bpm text":     ((425, 950, 560, 990),  dark),
    "L/M/H letters":   ((60, 1195, 230, 1340), dark),
    "knob pin":        ((115, 465, 190, 530),  dark),
    "walk icon fab":   ((50, 1255, 130, 1360), orange_on_dark),
}


def bbox(img, region, pred):
    x0, y0, x1, y1 = region
    sub = img[y0:y1, x0:x1]
    m = pred(sub)
    ys, xs = np.where(m)
    if len(xs) == 0:
        return None
    return xs.min() + x0, ys.min() + y0, xs.max() + x0, ys.max() + y0


print(f"{'item':16s} {'ref bbox':>22s} {'render bbox':>22s}   dx   dy   dw   dh")
for name, (reg, pred) in ITEMS.items():
    a, b = bbox(ref, reg, pred), bbox(cur, reg, pred)
    if not a or not b:
        print(f"{name:16s} {'missing':>22s}" if not a else f"{name:16s} {str(a):>22s} {'missing':>22s}")
        continue
    cx = lambda t: (t[0] + t[2]) / 2
    dx, dy = cx(b) - cx(a), b[3] - a[3]                 # centre x, baseline (bottom) y
    dw, dh = (b[2] - b[0]) - (a[2] - a[0]), (b[3] - b[1]) - (a[3] - a[1])
    print(f"{name:16s} {str(a):>22s} {str(b):>22s} {dx:+4.0f} {dy:+4.0f} {dw:+4.0f} {dh:+4.0f}")
