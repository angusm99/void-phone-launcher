"""Render the 512x512 Google Play hi-res icon for void phone.

Mirrors app/src/main/res/drawable/ic_launcher_{background,foreground}.xml: the "void" ring with a
terminal prompt inside, on AMOLED near-black. Coordinates are in the adaptive icon's 108-unit
viewport; only the central 72 units are visible on a launcher, so that is the area rendered here.

Usage:  python scripts/make_store_icon.py      ->  play-store/icon-512.png  (24-bit PNG, no alpha)
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

SIZE = 512
SS = 4                                # supersample for clean anti-aliased edges
PX = SIZE * SS
UNIT = PX / 72                        # visible adaptive-icon area is 72 of the 108 units


def p(v):                             # viewport units -> supersampled pixels
    return (v - 18) * UNIT


def hex_rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))


BG_CENTRE, BG_EDGE = hex_rgb("#0B1526"), hex_rgb("#020508")
RING, PROMPT = hex_rgb("#3B82F6"), hex_rgb("#60A5FA")

# Background: radial lift from the centre (gradientRadius 60 units, as in the vector)
mask = Image.radial_gradient("L").resize((round(120 * UNIT),) * 2, Image.BICUBIC)
offset = round(p(54) - 60 * UNIT)
fade = Image.new("L", (PX, PX), 255)
fade.paste(mask, (offset, offset))
img = Image.composite(Image.new("RGB", (PX, PX), BG_EDGE), Image.new("RGB", (PX, PX), BG_CENTRE), fade)


def ring(draw, r, width, rgba):
    half = r + width / 2
    draw.ellipse([p(54 - half), p(54 - half), p(54 + half), p(54 + half)], outline=rgba,
                 width=round(width * UNIT))


# Soft glow behind the ring (a touch of blur the vector can't do)
glow = Image.new("RGBA", (PX, PX), (0, 0, 0, 0))
ring(ImageDraw.Draw(glow), 27, 6.5, RING + (round(255 * 0.22),))
glow = glow.filter(ImageFilter.GaussianBlur(1.2 * UNIT))

art = Image.new("RGBA", (PX, PX), (0, 0, 0, 0))
d = ImageDraw.Draw(art)
ring(d, 27, 2.6, RING + (255,))


def stroke(points, width, rgb):       # polyline with round caps and joins
    pts = [(p(x), p(y)) for x, y in points]
    w = round(width * UNIT)
    d.line(pts, fill=rgb + (255,), width=w, joint="curve")
    for x, y in pts:
        d.ellipse([x - w / 2, y - w / 2, x + w / 2, y + w / 2], fill=rgb + (255,))


stroke([(43.5, 46), (51, 54), (43.5, 62)], 3.2, PROMPT)   # ">"
stroke([(55, 62), (64.5, 62)], 3.2, PROMPT)               # "_"

out = img.convert("RGBA")
out.alpha_composite(glow)
out.alpha_composite(art)
out = out.convert("RGB").resize((SIZE, SIZE), Image.LANCZOS)

dest = Path(__file__).resolve().parent.parent / "play-store" / "icon-512.png"
dest.parent.mkdir(exist_ok=True)
out.save(dest, optimize=True)
print(f"wrote {dest} {out.size} {out.mode}")
