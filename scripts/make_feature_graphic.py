"""Render the 1024x500 Google Play feature graphic for void phone.

The app-icon mark (void ring + terminal prompt) on the left, the lowercase wordmark and a
terminal-mode line on the right, on AMOLED near-black. Colours match the app's blue theme.

Usage:  python scripts/make_feature_graphic.py  ->  play-store/feature-1024x500.png  (24-bit, no alpha)
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

W, H, SS = 1024, 500, 2               # supersample for clean anti-aliased edges
PW, PH = W * SS, H * SS
FONTS = Path("C:/Windows/Fonts")
MARGIN = 72                           # keep text clear of the edges (Play crops/overlays them)


def hex_rgb(h):
    return tuple(int(h[i:i + 2], 16) for i in (1, 3, 5))


BG_CENTRE, BG_EDGE = hex_rgb("#0B1526"), hex_rgb("#020508")
RING, LIGHT, DIM, TEXT = hex_rgb("#3B82F6"), hex_rgb("#60A5FA"), hex_rgb("#1E40AF"), hex_rgb("#E6EEF8")


def radial_fade(radius):
    """L-mode mask: 0 at the centre, exactly 255 at `radius` (Pillow's own gradient only
    reaches 255 in its corners, which leaves a visible seam at the mask's edge)."""
    g = Image.radial_gradient("L").point(lambda v: min(255, round(v * 1.4142)))
    return g.resize((2 * radius, 2 * radius), Image.BICUBIC)


# Background: radial lift centred behind the icon mark
cx, cy, ring_r = 280 * SS, 250 * SS, 140 * SS
bg_r = 620 * SS
fade = Image.new("L", (PW, PH), 255)
fade.paste(radial_fade(bg_r), (cx - bg_r, cy - bg_r))
img = Image.composite(Image.new("RGB", (PW, PH), BG_EDGE),
                      Image.new("RGB", (PW, PH), BG_CENTRE), fade).convert("RGBA")

# Icon mark: same geometry as the adaptive icon (ring r=27, prompt), scaled to ring_r
u = ring_r / 27


def pt(x, y):                          # adaptive-icon viewport units -> pixels, centred on (cx, cy)
    return cx + (x - 54) * u, cy + (y - 54) * u


def ring(draw, r_units, w_units, rgba):
    half = (r_units + w_units / 2) * u
    draw.ellipse([cx - half, cy - half, cx + half, cy + half], outline=rgba, width=round(w_units * u))


glow = Image.new("RGBA", (PW, PH), (0, 0, 0, 0))
ring(ImageDraw.Draw(glow), 27, 6.5, RING + (70,))
img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(1.6 * u)))

art = Image.new("RGBA", (PW, PH), (0, 0, 0, 0))
d = ImageDraw.Draw(art)
ring(d, 27, 2.6, RING + (255,))


def stroke(points, width_units, rgb):  # polyline with round caps and joins
    pts = [pt(x, y) for x, y in points]
    w = round(width_units * u)
    d.line(pts, fill=rgb + (255,), width=w, joint="curve")
    for x, y in pts:
        d.ellipse([x - w / 2, y - w / 2, x + w / 2, y + w / 2], fill=rgb + (255,))


stroke([(43.5, 46), (51, 54), (43.5, 62)], 3.2, LIGHT)   # ">"
stroke([(55, 62), (64.5, 62)], 3.2, LIGHT)               # "_"
img.alpha_composite(art)

# Wordmark sized to fit between x0 and the right margin, then the terminal line and a tile row
t = ImageDraw.Draw(img)
x0 = 500 * SS
room = (W - MARGIN) * SS - x0
size = 104
while ImageFont.truetype(str(FONTS / "segoeuil.ttf"), size * SS).getlength("void phone") > room:
    size -= 2
word = ImageFont.truetype(str(FONTS / "segoeuil.ttf"), size * SS)
mono = ImageFont.truetype(str(FONTS / "consola.ttf"), 26 * SS)
t.text((x0, 200 * SS), "void phone", font=word, fill=TEXT + (255,), anchor="ls")
t.text((x0 + 4 * SS, 262 * SS), "C:\\VOID\\> essentials_only_", font=mono, fill=LIGHT + (255,), anchor="ls")

r_tile, gap = 21 * SS, 17 * SS          # a quiet row of outlined tiles, echoing the home screen
for i in range(5):
    tx, ty = x0 + 4 * SS + r_tile + i * (2 * r_tile + gap), 326 * SS
    t.ellipse([tx - r_tile, ty - r_tile, tx + r_tile, ty + r_tile],
              outline=(RING if i == 0 else DIM) + (255,), width=2 * SS)

out = img.convert("RGB").resize((W, H), Image.LANCZOS)
dest = Path(__file__).resolve().parent.parent / "play-store" / "feature-1024x500.png"
dest.parent.mkdir(exist_ok=True)
out.save(dest, optimize=True)
print(f"wrote {dest} {out.size} {out.mode} (wordmark {size}px)")
