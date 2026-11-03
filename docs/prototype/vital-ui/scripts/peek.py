"""Zoomed reference | render | diff for any region:  python3 -I scripts/peek.py x0 y0 x1 y1 [scale]  -> out/peek.png"""
import sys
import numpy as np
from PIL import Image
x0, y0, x1, y1 = map(int, sys.argv[1:5]); sc = float(sys.argv[5]) if len(sys.argv) > 5 else 2
a = Image.open("design/reference.png").convert("RGB").crop((x0, y0, x1, y1))
b = Image.open("out/render.png").convert("RGB").crop((x0, y0, x1, y1))
d = Image.fromarray(np.abs(np.array(a).astype(int) - np.array(b).astype(int)).clip(0, 255).astype("uint8")).point(lambda v: min(255, v * 3))
w, h = a.size
out = Image.new("RGB", (w * 3 + 16, h), (40, 40, 40))
for i, im in enumerate((a, b, d)): out.paste(im, (i * (w + 8), 0))
out = out.resize((int(out.width * sc), int(out.height * sc)), Image.LANCZOS); out.save("out/peek.png"); print(out.size)
