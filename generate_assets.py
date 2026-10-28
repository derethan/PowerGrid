import os
import json
import zipfile
import shutil
from PIL import Image, ImageDraw, ImageFilter

BASE_DIR = r"C:\Users\andg_\OneDrive\Desktop\minecraft\PowerGrid\src\main\resources\assets\powergrid"
BLOCKS_DIR_32 = os.path.join(BASE_DIR, "textures", "block")
ITEMS_DIR_32 = os.path.join(BASE_DIR, "textures", "item")
MODELS_BLOCK = os.path.join(BASE_DIR, "models", "block")
MODELS_ITEM = os.path.join(BASE_DIR, "models", "item")
BLOCKSTATES = os.path.join(BASE_DIR, "blockstates")
ITEM_DEFS = os.path.join(BASE_DIR, "items")

PACK_DIR = r"C:\Users\andg_\OneDrive\Desktop\minecraft\PowerGrid\resourcepacks\1024x_pack"
PACK_BLOCKS_DIR_1024 = os.path.join(PACK_DIR, "assets", "powergrid", "textures", "block")
PACK_ITEMS_DIR_1024 = os.path.join(PACK_DIR, "assets", "powergrid", "textures", "item")

OUTPUT_ZIP = r"C:\Users\andg_\OneDrive\Desktop\minecraft\PowerGrid\resourcepacks\(v1.0) 1024x Power Grid.zip"
CURSEFORGE_RP_DIR = r"C:\Users\andg_\curseforge\minecraft\Instances\Andrew\resourcepacks"

for d in [BLOCKS_DIR_32, ITEMS_DIR_32, MODELS_BLOCK, MODELS_ITEM, BLOCKSTATES, ITEM_DEFS, PACK_BLOCKS_DIR_1024, PACK_ITEMS_DIR_1024]:
    os.makedirs(d, exist_ok=True)

def write_json(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

# ==============================================================================
# PARAMETRIC TEXTURE RENDERING (Supports 32x32 and 1024x1024)
# Coordinates are expressed in standard Minecraft 16.0 grid units.
# ==============================================================================

def sc(val, size):
    return int(round(val * size / 16.0))

def rgb_clamp(c):
    return max(0, min(255, int(c)))

def color_mult(col, factor):
    return (rgb_clamp(col[0] * factor), rgb_clamp(col[1] * factor), rgb_clamp(col[2] * factor), col[3] if len(col) > 3 else 255)

def color_add(col, delta):
    return (rgb_clamp(col[0] + delta), rgb_clamp(col[1] + delta), rgb_clamp(col[2] + delta), col[3] if len(col) > 3 else 255)

def draw_rect(d, size, x1, y1, x2, y2, fill=None, outline=None, width=1):
    sx1, sy1 = sc(x1, size), sc(y1, size)
    sx2, sy2 = sc(x2, size), sc(y2, size)
    w = max(1, sc(width, size) if width > 1 else (1 if size == 32 else max(2, int(size / 256))))
    d.rectangle([sx1, sy1, sx2, sy2], fill=fill, outline=outline, width=w if outline else 0)

def draw_ellipse(d, size, x1, y1, x2, y2, fill=None, outline=None, width=1):
    sx1, sy1 = sc(x1, size), sc(y1, size)
    sx2, sy2 = sc(x2, size), sc(y2, size)
    w = max(1, sc(width, size) if width > 1 else (1 if size == 32 else max(2, int(size / 256))))
    d.ellipse([sx1, sy1, sx2, sy2], fill=fill, outline=outline, width=w if outline else 0)

def draw_line(d, size, x1, y1, x2, y2, fill, width=1):
    sx1, sy1 = sc(x1, size), sc(y1, size)
    sx2, sy2 = sc(x2, size), sc(y2, size)
    w = max(1, int(round(width * size / 16.0)))
    d.line([(sx1, sy1), (sx2, sy2)], fill=fill, width=w)

# --- TEXTURE GENERATORS ---

def render_treated_wood_pole(size):
    im = Image.new("RGBA", (size, size), (46, 31, 20, 255))
    d = ImageDraw.Draw(im)
    steps = size
    for x in range(steps):
        fx = x / float(steps)
        val = int(fx * 37) % 5
        shade = -8 if val == 0 else (6 if val == 1 else (-4 if val == 2 else (4 if val == 3 else 0)))
        if fx < 0.15 or fx > 0.85 or (0.45 < fx < 0.55):
            shade -= 12
        col = color_add((48, 32, 21), shade)
        d.line([(x, 0), (x, size - 1)], fill=col)

    for y_band in [3.0, 12.0]:
        draw_rect(d, size, 0, y_band, 16, y_band + 1.2, fill=(65, 70, 78))
        draw_line(d, size, 0, y_band, 16, y_band, fill=(90, 98, 108), width=0.3)
        draw_line(d, size, 0, y_band + 1.2, 16, y_band + 1.2, fill=(40, 44, 50), width=0.3)
        for rx in [2.5, 8.0, 13.5]:
            draw_ellipse(d, size, rx - 0.4, y_band + 0.2, rx + 0.4, y_band + 1.0, fill=(140, 150, 165), outline=(30, 32, 36))

    draw_rect(d, size, 12.5, 6.0, 15.5, 6.8, fill=(80, 85, 95), outline=(35, 38, 42))
    draw_ellipse(d, size, 12.0, 5.8, 13.0, 7.0, fill=(130, 140, 155))
    draw_rect(d, size, 0.5, 10.0, 3.5, 10.8, fill=(80, 85, 95), outline=(35, 38, 42))
    draw_ellipse(d, size, 3.0, 9.8, 4.0, 11.0, fill=(130, 140, 155))
    return im

def render_treated_wood_pole_end(size):
    im = Image.new("RGBA", (size, size), (42, 28, 18, 255))
    d = ImageDraw.Draw(im)
    for r in range(int(size * 0.48), 0, -max(1, int(size / 16))):
        c = (52, 35, 23) if (r // max(1, int(size / 32))) % 2 == 0 else (38, 25, 16)
        d.ellipse([size/2 - r, size/2 - r, size/2 + r, size/2 + r], outline=c, width=max(1, int(size/64)))
    cr = int(size * 0.15)
    d.ellipse([size/2 - cr, size/2 - cr, size/2 + cr, size/2 + cr], fill=(26, 17, 11))
    draw_ellipse(d, size, 0.5, 0.5, 15.5, 15.5, outline=(75, 82, 92), width=1.0)
    return im

def render_pole_crossarm(size):
    im = Image.new("RGBA", (size, size), (50, 34, 22, 255))
    d = ImageDraw.Draw(im)
    for y in range(size):
        fy = y / float(size)
        val = int(fy * 33) % 4
        shade = -7 if val == 0 else (6 if val == 1 else (-3 if val == 2 else 3))
        col = color_add((50, 34, 22), shade)
        d.line([(0, y), (size - 1, y)], fill=col)

    draw_rect(d, size, 6.0, 0, 10.0, 16, fill=(55, 60, 68), outline=(38, 42, 48))
    draw_line(d, size, 6.0, 0, 6.0, 16, fill=(85, 95, 105), width=0.4)
    draw_line(d, size, 10.0, 0, 10.0, 16, fill=(35, 38, 44), width=0.4)

    draw_rect(d, size, 0, 0, 1.8, 16, fill=(58, 64, 72), outline=(35, 38, 44))
    draw_rect(d, size, 14.2, 0, 16, 16, fill=(58, 64, 72), outline=(35, 38, 44))

    for bx, by in [(7.0, 4.0), (9.0, 4.0), (7.0, 12.0), (9.0, 12.0), (0.9, 8.0), (15.1, 8.0)]:
        draw_ellipse(d, size, bx - 0.5, by - 0.5, bx + 0.5, by + 0.5, fill=(140, 150, 165), outline=(30, 34, 40))
    return im

def render_pole_crossarm_end(size):
    im = Image.new("RGBA", (size, size), (46, 31, 20, 255))
    d = ImageDraw.Draw(im)
    for r in range(int(size * 0.45), 0, -max(1, int(size / 16))):
        c = (56, 38, 25) if (r // max(1, int(size / 32))) % 2 == 0 else (40, 27, 18)
        d.ellipse([size/2 - r, size/2 - r, size/2 + r, size/2 + r], outline=c, width=max(1, int(size/64)))
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(70, 76, 85), width=1.2)
    for rx, ry in [(1.8, 1.8), (14.2, 1.8), (1.8, 14.2), (14.2, 14.2)]:
        draw_ellipse(d, size, rx - 0.4, ry - 0.4, rx + 0.4, ry + 0.4, fill=(135, 145, 160))
    return im

def render_steel_bracket(size):
    im = Image.new("RGBA", (size, size), (52, 57, 65, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(75, 82, 92), width=1.0)
    draw_line(d, size, 0.5, 0.5, 15.5, 0.5, fill=(100, 110, 125), width=1.0)
    draw_line(d, size, 0.5, 15.5, 15.5, 15.5, fill=(30, 33, 38), width=1.0)
    draw_ellipse(d, size, 5.5, 5.5, 10.5, 10.5, fill=(40, 44, 50), outline=(85, 95, 110))
    draw_ellipse(d, size, 7.0, 7.0, 9.0, 9.0, fill=(130, 140, 155))
    return im

def render_steel_truss_pole(size):
    im = Image.new("RGBA", (size, size), (28, 30, 35, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0, 0, 16, 16, outline=(65, 72, 82), width=1.5)
    draw_rect(d, size, 1.8, 1.8, 14.2, 14.2, outline=(42, 46, 52), width=1.0)
    draw_line(d, size, 2.5, 2.5, 13.5, 13.5, fill=(72, 80, 92), width=1.2)
    draw_line(d, size, 2.5, 13.5, 13.5, 2.5, fill=(72, 80, 92), width=1.2)
    draw_rect(d, size, 6.0, 6.0, 10.0, 10.0, fill=(52, 58, 66), outline=(80, 90, 102))
    draw_ellipse(d, size, 7.2, 7.2, 8.8, 8.8, fill=(140, 150, 165), outline=(35, 38, 44))
    for rx, ry in [(1.2, 1.2), (14.8, 1.2), (1.2, 14.8), (14.8, 14.8)]:
        draw_ellipse(d, size, rx - 0.4, ry - 0.4, rx + 0.4, ry + 0.4, fill=(130, 140, 155))
    return im

def render_relay_bracket(size):
    im = Image.new("RGBA", (size, size), (42, 45, 52, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(65, 72, 82), width=1.0)
    draw_rect(d, size, 2.0, 2.0, 14.0, 14.0, fill=(35, 38, 44))
    for bx, by in [(3.5, 3.5), (12.5, 3.5), (3.5, 12.5), (12.5, 12.5)]:
        draw_ellipse(d, size, bx - 1.2, by - 1.2, bx + 1.2, by + 1.2, fill=(20, 22, 25), outline=(80, 90, 105))
        draw_ellipse(d, size, bx - 0.5, by - 0.5, bx + 0.5, by + 0.5, fill=(130, 140, 155))
    draw_ellipse(d, size, 6.0, 6.0, 10.0, 10.0, fill=(55, 60, 70), outline=(85, 95, 110))
    return im

def render_insulator(size, base_col, highlight_col, shadow_col):
    im = Image.new("RGBA", (size, size), base_col)
    d = ImageDraw.Draw(im)
    for y in range(size):
        fy = y / float(size)
        rib = int(fy * 16) % 4
        c = highlight_col if rib == 0 else (base_col if rib == 1 else shadow_col)
        d.line([(0, y), (size - 1, y)], fill=c)

    spec_w = max(1, int(size * 0.15))
    spec_x = int(size * 0.28)
    for x in range(spec_x, spec_x + spec_w):
        col = color_add(highlight_col, 35)
        d.line([(x, 0), (x, size - 1)], fill=col)
    return im

def render_wire_clamp(size, base_col, highlight_col):
    im = Image.new("RGBA", (size, size), base_col)
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=color_add(base_col, -30), width=1.0)
    draw_rect(d, size, 0, 6.0, 16, 10.0, fill=color_add(base_col, -40))
    draw_line(d, size, 0, 6.0, 16, 6.0, fill=highlight_col, width=0.5)
    draw_line(d, size, 0, 10.0, 16, 10.0, fill=color_add(base_col, -60), width=0.5)
    draw_ellipse(d, size, 3.5, 7.0, 5.5, 9.0, fill=highlight_col, outline=(25, 25, 25))
    draw_ellipse(d, size, 10.5, 7.0, 12.5, 9.0, fill=highlight_col, outline=(25, 25, 25))
    return im

def render_connector_base(size):
    im = Image.new("RGBA", (size, size), (38, 42, 48, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(70, 78, 90), width=1.0)
    draw_rect(d, size, 2.5, 2.5, 13.5, 13.5, fill=(28, 31, 36))
    draw_ellipse(d, size, 4.5, 4.5, 11.5, 11.5, fill=(50, 55, 65), outline=(90, 100, 115))
    for bx, by in [(2.0, 2.0), (14.0, 2.0), (2.0, 14.0), (14.0, 14.0)]:
        draw_ellipse(d, size, bx - 1.0, by - 1.0, bx + 1.0, by + 1.0, fill=(130, 140, 155), outline=(25, 28, 32))
    return im

def render_connector_terminal(size):
    im = Image.new("RGBA", (size, size), (190, 120, 50, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(140, 80, 30), width=1.0)
    for y in range(0, size, max(1, int(size / 16))):
        c = (230, 160, 80) if (y // max(1, int(size / 32))) % 2 == 0 else (160, 95, 35)
        d.line([(0, y), (size - 1, y)], fill=c)
    draw_ellipse(d, size, 3.5, 3.5, 12.5, 12.5, fill=(215, 155, 60), outline=(100, 60, 20))
    draw_ellipse(d, size, 6.0, 6.0, 10.0, 10.0, fill=(255, 200, 100))
    return im

def render_transformer_front(size):
    im = Image.new("RGBA", (size, size), (45, 50, 58, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(75, 84, 98), width=1.0)
    draw_rect(d, size, 1.5, 1.5, 14.5, 14.5, fill=(35, 38, 45))
    draw_rect(d, size, 3.0, 3.0, 13.0, 7.5, fill=(185, 150, 75), outline=(95, 75, 35))
    draw_line(d, size, 4.5, 4.5, 11.5, 4.5, fill=(50, 40, 20), width=0.4)
    draw_line(d, size, 4.5, 6.0, 11.5, 6.0, fill=(50, 40, 20), width=0.4)
    draw_ellipse(d, size, 5.0, 9.0, 11.0, 15.0, fill=(225, 220, 205), outline=(70, 78, 90))
    draw_line(d, size, 8.0, 12.0, 10.0, 10.2, fill=(180, 30, 30), width=0.6)
    draw_ellipse(d, size, 7.5, 11.5, 8.5, 12.5, fill=(30, 30, 30))
    return im

def render_transformer_side(size):
    im = Image.new("RGBA", (size, size), (42, 46, 54, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(72, 80, 94), width=1.0)
    fin_count = 8
    fin_w = size / float(fin_count)
    for i in range(fin_count):
        x_start = int(i * fin_w)
        x_mid = int(x_start + fin_w * 0.45)
        x_end = int(x_start + fin_w)
        d.rectangle([x_start, sc(2.0, size), x_mid, sc(14.0, size)], fill=(65, 72, 84))
        d.rectangle([x_mid, sc(2.0, size), x_end, sc(14.0, size)], fill=(28, 30, 36))
    return im

def render_transformer_top(size):
    im = Image.new("RGBA", (size, size), (48, 54, 62, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(78, 86, 100), width=1.0)
    draw_ellipse(d, size, 3.5, 3.5, 12.5, 12.5, fill=(38, 42, 50), outline=(85, 95, 110))
    for i in range(8):
        import math
        ang = i * (math.pi / 4)
        bx = 8.0 + 3.3 * math.cos(ang)
        by = 8.0 + 3.3 * math.sin(ang)
        draw_ellipse(d, size, bx - 0.4, by - 0.4, bx + 0.4, by + 0.4, fill=(135, 145, 160))
    return im

# --- HEAVY DUTY SAFETY SWITCH / DISCONNECT BOX TEXTURES (Siemens-style) ---

def render_breaker_box_front(size, is_on):
    # Light industrial powder-coat grey (ANSI 61 grey)
    im = Image.new("RGBA", (size, size), (204, 208, 214, 255))
    d = ImageDraw.Draw(im)
    # Outer door beveled edge
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(145, 150, 158), width=1.0)
    draw_line(d, size, 0.5, 0.5, 15.5, 0.5, fill=(238, 242, 248), width=0.8)
    draw_line(d, size, 0.5, 0.5, 0.5, 15.5, fill=(238, 242, 248), width=0.8)
    draw_line(d, size, 0.5, 15.5, 15.5, 15.5, fill=(130, 135, 142), width=0.8)
    draw_line(d, size, 15.5, 0.5, 15.5, 15.5, fill=(130, 135, 142), width=0.8)

    # Left hinges (2 heavy sheet metal barrel hinges on door edge)
    for hy in [3.0, 11.5]:
        draw_rect(d, size, 0.2, hy, 1.4, hy + 2.0, fill=(160, 166, 175), outline=(100, 105, 112))
        draw_ellipse(d, size, 0.5, hy + 0.2, 1.1, hy + 0.8, fill=(210, 215, 222))

    # Stamped raised center door panel
    draw_rect(d, size, 2.5, 2.2, 13.5, 13.8, fill=(195, 200, 206), outline=(150, 155, 164))
    draw_line(d, size, 2.5, 2.2, 13.5, 2.2, fill=(235, 240, 246), width=0.6)
    draw_line(d, size, 2.5, 2.2, 2.5, 13.8, fill=(235, 240, 246), width=0.6)
    draw_line(d, size, 2.5, 13.8, 13.5, 13.8, fill=(135, 140, 148), width=0.6)
    draw_line(d, size, 13.5, 2.2, 13.5, 13.8, fill=(135, 140, 148), width=0.6)

    # Stamped manufacturer header on upper bevel ("POWER GRID / SAFETY SWITCH")
    draw_rect(d, size, 4.0, 2.6, 12.0, 3.8, fill=(185, 190, 198))
    draw_line(d, size, 4.5, 3.2, 11.5, 3.2, fill=(148, 152, 160), width=0.5)

    # Center Instruction / Warning Placard
    draw_rect(d, size, 4.2, 4.4, 11.8, 12.6, fill=(245, 245, 248), outline=(30, 32, 36), width=1.0)

    # ON Section at top of placard
    if is_on:
        draw_rect(d, size, 4.5, 4.6, 11.5, 6.6, fill=(35, 170, 50)) # Green active ON badge
        draw_rect(d, size, 6.2, 5.0, 9.8, 6.2, fill=(255, 255, 255))
    else:
        draw_rect(d, size, 4.5, 4.6, 11.5, 6.6, fill=(225, 228, 232))
        draw_rect(d, size, 6.5, 5.2, 9.5, 6.0, fill=(60, 64, 72))

    # Middle text / Rating / DANGER Warning label
    draw_line(d, size, 4.5, 6.8, 11.5, 6.8, fill=(40, 44, 50), width=0.5)
    draw_line(d, size, 5.2, 7.3, 10.8, 7.3, fill=(30, 32, 36), width=0.4)
    draw_line(d, size, 5.5, 8.0, 10.5, 8.0, fill=(60, 64, 72), width=0.3)
    # DANGER Banner with arc flash / lightning bolt
    draw_rect(d, size, 4.8, 8.6, 11.2, 10.0, fill=(215, 35, 35))
    draw_rect(d, size, 5.5, 8.9, 7.2, 9.7, fill=(255, 255, 255))
    draw_line(d, size, 7.6, 9.3, 10.8, 9.3, fill=(255, 255, 255), width=0.4)

    # OFF Section at bottom of placard
    if not is_on:
        draw_rect(d, size, 4.5, 10.4, 11.5, 12.4, fill=(225, 40, 40)) # Red active OFF badge
        draw_rect(d, size, 6.0, 10.8, 10.0, 12.0, fill=(255, 255, 255))
    else:
        draw_rect(d, size, 4.5, 10.4, 11.5, 12.4, fill=(225, 228, 232))
        draw_rect(d, size, 6.5, 11.0, 9.5, 11.8, fill=(60, 64, 72))

    # Door latch clasp / padlock bracket at bottom right
    draw_rect(d, size, 11.8, 11.5, 13.0, 13.2, fill=(120, 125, 135), outline=(60, 64, 70))
    draw_ellipse(d, size, 12.1, 12.0, 12.7, 12.8, fill=(40, 44, 50))
    return im

def render_breaker_box_side(size):
    im = Image.new("RGBA", (size, size), (195, 200, 206, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(145, 150, 158), width=1.0)
    draw_line(d, size, 2.5, 0.5, 2.5, 15.5, fill=(130, 135, 144), width=0.8)
    draw_line(d, size, 2.8, 0.5, 2.8, 15.5, fill=(225, 230, 236), width=0.4)

    for ky in [4.5, 11.5]:
        draw_ellipse(d, size, 6.0, ky - 2.0, 10.0, ky + 2.0, fill=(180, 185, 192), outline=(135, 140, 148))
        draw_ellipse(d, size, 6.8, ky - 1.2, 9.2, ky + 1.2, outline=(150, 155, 164))

    draw_rect(d, size, 14.5, 0.5, 15.5, 15.5, fill=(160, 165, 174))
    for fy in [1.5, 14.5]:
        draw_ellipse(d, size, 14.7, fy - 0.4, 15.3, fy + 0.4, fill=(50, 55, 62))
    return im

def render_breaker_box_top(size):
    im = Image.new("RGBA", (size, size), (198, 203, 210, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(148, 153, 160), width=1.0)
    draw_ellipse(d, size, 5.0, 5.0, 11.0, 11.0, fill=(175, 180, 188), outline=(125, 130, 138))
    draw_ellipse(d, size, 6.0, 6.0, 10.0, 10.0, fill=(155, 160, 168), outline=(95, 100, 108))
    draw_rect(d, size, 0.5, 14.5, 15.5, 15.5, fill=(160, 165, 174))
    return im

def render_breaker_handle(size):
    im = Image.new("RGBA", (size, size), (135, 142, 152, 255))
    d = ImageDraw.Draw(im)
    # Bright RED molded ergonomic safety grip handle
    draw_rect(d, size, 1.5, 0, 14.5, 16, fill=(215, 30, 30), outline=(140, 18, 18))
    for y in range(0, size, max(1, int(size / 8))):
        draw_line(d, size, 1.5, y * 16.0 / size, 5.5, y * 16.0 / size, fill=(255, 80, 80), width=0.6)
    draw_rect(d, size, 5.5, 3.0, 12.5, 13.0, fill=(175, 22, 22))
    draw_ellipse(d, size, 7.5, 6.5, 10.5, 9.5, fill=(210, 218, 228), outline=(75, 82, 92))
    return im

def render_heavy_accumulator_side(size):
    im = Image.new("RGBA", (size, size), (44, 48, 56, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(75, 82, 95), width=1.0)
    sw = size / 8.0
    for i in range(8):
        c = (235, 195, 35) if i % 2 == 0 else (28, 30, 35)
        d.polygon([(i*sw, sc(12.0, size)), ((i+1)*sw, sc(12.0, size)), ((i+0.5)*sw, sc(15.5, size)), ((i-0.5)*sw, sc(15.5, size))], fill=c)
    draw_rect(d, size, 11.5, 2.5, 13.5, 10.5, fill=(18, 20, 24), outline=(85, 95, 110))
    draw_rect(d, size, 12.0, 5.0, 13.0, 10.0, fill=(45, 180, 225))
    draw_ellipse(d, size, 3.5, 3.5, 8.5, 8.5, fill=(225, 220, 205), outline=(70, 78, 90))
    draw_line(d, size, 6.0, 6.0, 7.5, 4.8, fill=(180, 30, 30), width=0.5)
    return im

def render_heavy_accumulator_top(size):
    im = Image.new("RGBA", (size, size), (46, 50, 58, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(78, 86, 98), width=1.0)
    draw_ellipse(d, size, 2.5, 6.0, 6.5, 10.0, fill=(180, 30, 30), outline=(90, 15, 15))
    draw_ellipse(d, size, 3.5, 7.0, 5.5, 9.0, fill=(235, 175, 60))
    draw_ellipse(d, size, 9.5, 6.0, 13.5, 10.0, fill=(25, 28, 32), outline=(65, 72, 82))
    draw_ellipse(d, size, 10.5, 7.0, 12.5, 9.0, fill=(150, 160, 175))
    draw_ellipse(d, size, 6.5, 6.5, 9.5, 9.5, fill=(60, 66, 76), outline=(90, 100, 115))
    return im

def render_power_plant_controller(size):
    im = Image.new("RGBA", (size, size), (38, 42, 50, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(72, 80, 95), width=1.0)
    draw_rect(d, size, 1.5, 1.5, 14.5, 14.5, fill=(28, 32, 38))
    draw_ellipse(d, size, 2.5, 2.5, 7.5, 7.5, fill=(230, 225, 210), outline=(80, 90, 105))
    draw_line(d, size, 5.0, 5.0, 6.5, 3.8, fill=(190, 25, 25), width=0.5)
    draw_ellipse(d, size, 8.5, 2.5, 13.5, 7.5, fill=(230, 225, 210), outline=(80, 90, 105))
    draw_line(d, size, 11.0, 5.0, 12.2, 4.0, fill=(190, 25, 25), width=0.5)
    draw_ellipse(d, size, 3.0, 9.5, 5.0, 11.5, fill=(40, 215, 60), outline=(15, 90, 25))
    draw_ellipse(d, size, 7.0, 9.5, 9.0, 11.5, fill=(245, 185, 30), outline=(110, 80, 12))
    draw_ellipse(d, size, 11.0, 9.5, 13.0, 11.5, fill=(225, 35, 35), outline=(100, 15, 15))
    draw_ellipse(d, size, 6.0, 12.5, 10.0, 15.5, fill=(50, 55, 65), outline=(95, 105, 120))
    draw_rect(d, size, 7.5, 12.0, 8.5, 16.0, fill=(185, 145, 60))
    return im

def render_generator_casing(size):
    im = Image.new("RGBA", (size, size), (45, 48, 55, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(75, 82, 95), width=1.0)
    draw_rect(d, size, 2.5, 2.5, 13.5, 13.5, fill=(35, 38, 44), outline=(60, 66, 78))
    for ly in [4.5, 6.5, 8.5, 10.5]:
        draw_rect(d, size, 4.0, ly, 12.0, ly + 1.0, fill=(20, 22, 26))
        draw_line(d, size, 4.0, ly, 12.0, ly, fill=(65, 72, 85), width=0.4)
    for bx, by in [(1.5, 1.5), (14.5, 1.5), (1.5, 14.5), (14.5, 14.5), (8.0, 1.5), (8.0, 14.5)]:
        draw_ellipse(d, size, bx - 0.4, by - 0.4, bx + 0.4, by + 0.4, fill=(130, 140, 155))
    return im

def render_alternator_coil(size):
    im = Image.new("RGBA", (size, size), (32, 34, 40, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(65, 72, 84), width=1.0)
    coil_count = 10
    step = 14.0 / coil_count
    for i in range(coil_count):
        y = 1.0 + i * step
        draw_rect(d, size, 2.0, y, 14.0, y + step * 0.85, fill=(205, 115, 45), outline=(130, 65, 20))
        draw_line(d, size, 2.0, y + step * 0.2, 14.0, y + step * 0.2, fill=(255, 170, 85), width=0.4)
    return im

def render_turbine_rotor(size):
    im = Image.new("RGBA", (size, size), (36, 40, 48, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(70, 78, 92), width=1.0)
    for i in range(8):
        import math
        ang = i * (math.pi / 4)
        x1 = 8.0 + 2.0 * math.cos(ang)
        y1 = 8.0 + 2.0 * math.sin(ang)
        x2 = 8.0 + 6.5 * math.cos(ang + 0.4)
        y2 = 8.0 + 6.5 * math.sin(ang + 0.4)
        draw_line(d, size, x1, y1, x2, y2, fill=(150, 165, 185), width=1.2)
        draw_line(d, size, x1, y1, x2, y2, fill=(80, 90, 105), width=0.5)
    draw_ellipse(d, size, 5.5, 5.5, 10.5, 10.5, fill=(55, 62, 72), outline=(95, 105, 122))
    draw_ellipse(d, size, 7.0, 7.0, 9.0, 9.0, fill=(130, 140, 155))
    return im

def render_exhaust_stack_side(size):
    im = Image.new("RGBA", (size, size), (28, 30, 35, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(60, 66, 76), width=1.0)
    for y_seam in [5.0, 11.0]:
        draw_line(d, size, 0, y_seam, 16, y_seam, fill=(75, 82, 95), width=0.8)
        draw_line(d, size, 0, y_seam + 0.8, 16, y_seam + 0.8, fill=(15, 16, 20), width=0.5)
        for rx in [1.5, 4.5, 8.0, 11.5, 14.5]:
            draw_ellipse(d, size, rx - 0.4, y_seam - 0.4, rx + 0.4, y_seam + 0.4, fill=(120, 130, 145))
    return im

def render_exhaust_stack_top(size):
    im = Image.new("RGBA", (size, size), (32, 35, 42, 255))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 0.5, 0.5, 15.5, 15.5, outline=(65, 72, 84), width=1.0)
    draw_ellipse(d, size, 2.0, 2.0, 14.0, 14.0, fill=(12, 14, 18), outline=(80, 90, 105))
    draw_ellipse(d, size, 4.0, 4.0, 12.0, 12.0, fill=(5, 6, 8))
    draw_line(d, size, 2.0, 8.0, 14.0, 8.0, fill=(60, 68, 78), width=0.6)
    draw_line(d, size, 8.0, 2.0, 8.0, 14.0, fill=(60, 68, 78), width=0.6)
    return im

# --- ITEM TEXTURES ---

def render_engineer_hammer(size):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    draw_line(d, size, 3.0, 13.0, 11.0, 5.0, fill=(145, 95, 55), width=1.5)
    draw_line(d, size, 3.0, 13.0, 11.0, 5.0, fill=(195, 135, 85), width=0.6)
    draw_line(d, size, 9.5, 6.5, 11.5, 4.5, fill=(215, 160, 60), width=1.8)
    draw_line(d, size, 8.0, 2.5, 13.5, 8.0, fill=(75, 82, 94), width=3.0)
    draw_line(d, size, 7.5, 2.0, 14.0, 8.5, fill=(130, 140, 155), width=1.0)
    draw_ellipse(d, size, 6.5, 1.5, 9.0, 4.0, fill=(160, 172, 190))
    draw_ellipse(d, size, 12.0, 7.0, 14.5, 9.5, fill=(160, 172, 190))
    return im

def render_voltmeter(size):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    draw_rect(d, size, 2.5, 2.0, 13.5, 14.0, fill=(50, 36, 25), outline=(180, 135, 55))
    draw_ellipse(d, size, 4.0, 3.5, 12.0, 9.5, fill=(235, 230, 215), outline=(85, 95, 110))
    draw_line(d, size, 8.0, 8.0, 10.5, 5.0, fill=(190, 25, 25), width=0.8)
    draw_ellipse(d, size, 7.5, 7.5, 8.5, 8.5, fill=(30, 30, 30))
    draw_ellipse(d, size, 4.5, 11.0, 6.5, 13.0, fill=(200, 35, 35), outline=(60, 10, 10))
    draw_ellipse(d, size, 9.5, 11.0, 11.5, 13.0, fill=(30, 32, 38), outline=(70, 78, 88))
    return im

def render_wire_cutters(size):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    draw_line(d, size, 2.5, 13.5, 7.0, 8.5, fill=(205, 35, 35), width=2.0)
    draw_line(d, size, 13.5, 13.5, 9.0, 8.5, fill=(205, 35, 35), width=2.0)
    draw_line(d, size, 2.5, 13.5, 7.0, 8.5, fill=(245, 75, 75), width=0.8)
    draw_line(d, size, 13.5, 13.5, 9.0, 8.5, fill=(245, 75, 75), width=0.8)
    draw_ellipse(d, size, 6.8, 6.8, 9.2, 9.2, fill=(60, 66, 76), outline=(130, 140, 155))
    draw_line(d, size, 7.0, 7.0, 5.0, 2.5, fill=(140, 150, 165), width=2.2)
    draw_line(d, size, 9.0, 7.0, 11.0, 2.5, fill=(140, 150, 165), width=2.2)
    draw_line(d, size, 6.5, 6.0, 5.5, 2.5, fill=(225, 235, 245), width=0.8)
    draw_line(d, size, 9.5, 6.0, 10.5, 2.5, fill=(225, 235, 245), width=0.8)
    return im

def render_wire_coil(size, wire_col, wire_hi, steel_spool=False):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    flange_col = (55, 60, 70) if steel_spool else (85, 55, 32)
    flange_edge = (90, 100, 115) if steel_spool else (125, 80, 48)
    draw_ellipse(d, size, 1.5, 1.5, 14.5, 6.5, fill=flange_col, outline=flange_edge)
    draw_ellipse(d, size, 1.5, 9.5, 14.5, 14.5, fill=flange_col, outline=flange_edge)
    draw_rect(d, size, 2.5, 4.0, 13.5, 12.0, fill=wire_col)
    for y in range(sc(4.0, size), sc(12.0, size), max(1, int(size / 16))):
        c = wire_hi if (y // max(1, int(size / 32))) % 2 == 0 else wire_col
        d.line([(sc(2.5, size), y), (sc(13.5, size), y)], fill=c)
    draw_ellipse(d, size, 6.5, 3.0, 9.5, 5.0, fill=(20, 22, 26))
    return im

def render_material_item(size, item_name):
    im = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    if item_name == "treated_wood":
        draw_rect(d, size, 2.0, 4.0, 14.0, 8.0, fill=(48, 32, 20), outline=(26, 17, 10))
        draw_line(d, size, 2.0, 4.0, 14.0, 4.0, fill=(75, 50, 32), width=0.6)
        draw_rect(d, size, 2.0, 9.0, 14.0, 13.0, fill=(48, 32, 20), outline=(26, 17, 10))
        draw_line(d, size, 2.0, 9.0, 14.0, 9.0, fill=(75, 50, 32), width=0.6)
    elif item_name == "copper_wire":
        draw_ellipse(d, size, 3.0, 3.0, 13.0, 13.0, outline=(215, 125, 45), width=2.0)
        draw_ellipse(d, size, 3.5, 3.5, 12.5, 12.5, outline=(255, 170, 80), width=0.8)
    elif item_name == "electrum_wire":
        draw_ellipse(d, size, 3.0, 3.0, 13.0, 13.0, outline=(225, 185, 45), width=2.0)
        draw_ellipse(d, size, 3.5, 3.5, 12.5, 12.5, outline=(255, 230, 110), width=0.8)
    elif item_name == "steel_strand":
        draw_ellipse(d, size, 3.0, 3.0, 13.0, 13.0, outline=(90, 100, 115), width=2.2)
        draw_ellipse(d, size, 3.5, 3.5, 12.5, 12.5, outline=(155, 170, 190), width=0.8)
    elif item_name == "ceramic_insulator":
        draw_ellipse(d, size, 3.5, 8.0, 12.5, 13.0, fill=(230, 225, 215), outline=(130, 130, 130))
        draw_ellipse(d, size, 5.0, 4.0, 11.0, 9.0, fill=(245, 240, 230), outline=(130, 130, 130))
        draw_ellipse(d, size, 6.5, 2.0, 9.5, 5.0, fill=(75, 82, 92))
    return im

# --- PACK ICON GENERATOR ---

def render_pack_icon():
    size = 1024
    im = Image.new("RGBA", (size, size), (24, 28, 36, 255))
    d = ImageDraw.Draw(im)
    for y in range(size):
        fy = y / float(size)
        r = int(35 * (1 - fy) + 210 * fy * (1 - fy) * 2 + 15 * fy)
        g = int(38 * (1 - fy) + 120 * fy * (1 - fy) * 2 + 20 * fy)
        b = int(55 * (1 - fy) + 60 * fy * (1 - fy) * 2 + 35 * fy)
        d.line([(0, y), (size - 1, y)], fill=(r, g, b))

    px = size // 2
    d.rectangle([px - 28, int(size * 0.22), px + 28, size], fill=(32, 22, 15))
    for cy in [int(size * 0.30), int(size * 0.44)]:
        d.rectangle([px - 380, cy - 22, px + 380, cy + 22], fill=(42, 28, 18))
        for ix in [px - 340, px - 180, px + 180, px + 340]:
            d.rectangle([ix - 18, cy - 65, ix + 18, cy - 22], fill=(230, 225, 215))

    for cy, sag in [(int(size * 0.25), 60), (int(size * 0.39), 55)]:
        for ix in [px - 340, px - 180, px + 180, px + 340]:
            points = [(0, cy - 40), (ix // 2, cy + sag), (ix, cy), ((ix + size) // 2, cy + sag), (size, cy - 40)]
            d.line(points, fill=(20, 22, 25), width=8)

    d.ellipse([px - 180 - 30, int(size * 0.25) - 30, px - 180 + 30, int(size * 0.25) + 30], fill=(120, 220, 255, 160))
    d.ellipse([px - 180 - 10, int(size * 0.25) - 10, px - 180 + 10, int(size * 0.25) + 10], fill=(255, 255, 255, 255))

    d.rectangle([0, int(size * 0.76), size, int(size * 0.96)], fill=(16, 18, 22, 240))
    d.line([(0, int(size * 0.76)), (size, int(size * 0.76))], fill=(245, 175, 45), width=10)
    d.line([(0, int(size * 0.96)), (size, int(size * 0.96))], fill=(245, 175, 45), width=10)

    return im

# ==============================================================================
# BATCH GENERATION OF TEXTURES (32x32 & 1024x1024)
# ==============================================================================

TEXTURE_DEFS = {
    "block": [
        ("treated_wood_pole", render_treated_wood_pole),
        ("treated_wood_pole_end", render_treated_wood_pole_end),
        ("pole_crossarm", render_pole_crossarm),
        ("pole_crossarm_end", render_pole_crossarm_end),
        ("steel_bracket", render_steel_bracket),
        ("steel_truss_pole", render_steel_truss_pole),
        ("relay_bracket", render_relay_bracket),
        ("relay_insulator_lv", lambda s: render_insulator(s, (230, 225, 215), (255, 250, 242), (180, 175, 165))),
        ("relay_insulator_mv", lambda s: render_insulator(s, (190, 115, 65), (235, 155, 95), (140, 75, 35))),
        ("relay_insulator_hv", lambda s: render_insulator(s, (120, 135, 155), (175, 190, 210), (75, 90, 110))),
        ("wire_clamp_lv", lambda s: render_wire_clamp(s, (205, 110, 45), (255, 165, 90))),
        ("wire_clamp_mv", lambda s: render_wire_clamp(s, (225, 180, 50), (255, 220, 110))),
        ("wire_clamp_hv", lambda s: render_wire_clamp(s, (110, 120, 135), (175, 185, 200))),
        ("connector_base", render_connector_base),
        ("connector_bushing_lv", lambda s: render_insulator(s, (220, 215, 205), (250, 245, 235), (170, 165, 155))),
        ("connector_bushing_mv", lambda s: render_insulator(s, (185, 110, 60), (230, 150, 90), (135, 70, 30))),
        ("connector_bushing_hv", lambda s: render_insulator(s, (115, 130, 150), (170, 185, 205), (70, 85, 105))),
        ("connector_terminal", render_connector_terminal),
        ("transformer_front", render_transformer_front),
        ("transformer_side", render_transformer_side),
        ("transformer_top", render_transformer_top),
        ("breaker_box_front_on", lambda s: render_breaker_box_front(s, True)),
        ("breaker_box_front_off", lambda s: render_breaker_box_front(s, False)),
        ("breaker_box_side", render_breaker_box_side),
        ("breaker_box_top", render_breaker_box_top),
        ("breaker_handle", render_breaker_handle),
        ("heavy_accumulator_side", render_heavy_accumulator_side),
        ("heavy_accumulator_top", render_heavy_accumulator_top),
        ("power_plant_controller", render_power_plant_controller),
        ("generator_casing", render_generator_casing),
        ("alternator_coil", render_alternator_coil),
        ("turbine_rotor", render_turbine_rotor),
        ("exhaust_stack_side", render_exhaust_stack_side),
        ("exhaust_stack_top", render_exhaust_stack_top),
    ],
    "item": [
        ("engineer_hammer", render_engineer_hammer),
        ("voltmeter", render_voltmeter),
        ("wire_cutters", render_wire_cutters),
        ("lv_wire_coil", lambda s: render_wire_coil(s, (205, 110, 45), (255, 165, 90), steel_spool=False)),
        ("mv_wire_coil", lambda s: render_wire_coil(s, (225, 180, 50), (255, 220, 110), steel_spool=False)),
        ("hv_wire_coil", lambda s: render_wire_coil(s, (130, 140, 155), (190, 200, 215), steel_spool=True)),
        ("treated_wood", lambda s: render_material_item(s, "treated_wood")),
        ("copper_wire", lambda s: render_material_item(s, "copper_wire")),
        ("electrum_wire", lambda s: render_material_item(s, "electrum_wire")),
        ("steel_strand", lambda s: render_material_item(s, "steel_strand")),
        ("ceramic_insulator", lambda s: render_material_item(s, "ceramic_insulator")),
    ]
}

print("Rendering 32x32 textures for default mod...")
for category, items in TEXTURE_DEFS.items():
    out_dir = BLOCKS_DIR_32 if category == "block" else ITEMS_DIR_32
    for name, func in items:
        im32 = func(32)
        im32.save(os.path.join(out_dir, f"{name}.png"))

print("Rendering 1024x1024 textures for Ultra-HD resource pack...")
for category, items in TEXTURE_DEFS.items():
    out_dir = PACK_BLOCKS_DIR_1024 if category == "block" else PACK_ITEMS_DIR_1024
    for name, func in items:
        im1024 = func(1024)
        im1024.save(os.path.join(out_dir, f"{name}.png"))

# Create pack.mcmeta and pack.png for 1024x Resource Pack
pack_meta = {
    "pack": {
        "pack_format": 84,
        "supported_formats": [15, 255],
        "min_format": 15,
        "max_format": 255,
        "description": "§6Power Grid §f- 1024x Ultra-HD Pack"
    }
}
write_json(os.path.join(PACK_DIR, "pack.mcmeta"), pack_meta)
pack_icon = render_pack_icon()
pack_icon.save(os.path.join(PACK_DIR, "pack.png"))

print("Zipping 1024x resource pack...")
os.makedirs(os.path.dirname(OUTPUT_ZIP), exist_ok=True)
with zipfile.ZipFile(OUTPUT_ZIP, "w", zipfile.ZIP_DEFLATED) as zipf:
    for root, dirs, files in os.walk(PACK_DIR):
        for file in files:
            full_p = os.path.join(root, file)
            rel_p = os.path.relpath(full_p, PACK_DIR)
            zipf.write(full_p, rel_p)

if os.path.exists(CURSEFORGE_RP_DIR):
    shutil.copy2(OUTPUT_ZIP, os.path.join(CURSEFORGE_RP_DIR, os.path.basename(OUTPUT_ZIP)))
    print(f"Copied resource pack to {CURSEFORGE_RP_DIR}")

# ==============================================================================
# 3D BLOCK MODEL JSON BUILDERS
# ==============================================================================

def make_element(x1, y1, z1, x2, y2, z2, tex, uv=None, cullface=None):
    faces = {}
    for face in ["down", "up", "north", "south", "west", "east"]:
        f_data = {"texture": f"#{tex}"}
        if cullface and face in cullface:
            f_data["cullface"] = face
        faces[face] = f_data
    return {
        "from": [x1, y1, z1],
        "to": [x2, y2, z2],
        "faces": faces
    }

# 1. Treated Wood Pole Models
def build_pole_elements(top_cap=False, bottom_base=False):
    elements = [
        make_element(4.5, 0, 5.0, 11.5, 16, 11.0, "pole"),
        make_element(5.0, 0, 4.5, 11.0, 16, 11.5, "pole"),
        make_element(4.2, 3.0, 4.7, 11.8, 4.0, 11.3, "bracket"),
        make_element(4.7, 3.0, 4.2, 11.3, 4.0, 11.8, "bracket"),
        make_element(4.2, 12.0, 4.7, 11.8, 13.0, 11.3, "bracket"),
        make_element(4.7, 12.0, 4.2, 11.3, 13.0, 11.8, "bracket"),
        make_element(11.5, 6.0, 7.5, 13.5, 6.8, 8.5, "bracket"),
        make_element(2.5, 10.0, 7.5, 4.5, 10.8, 8.5, "bracket")
    ]
    if top_cap:
        elements.append(make_element(4.8, 15.8, 4.8, 11.2, 16.3, 11.2, "end", cullface=["up"]))
    if bottom_base:
        elements.append(make_element(4.0, 0.0, 4.0, 12.0, 2.0, 12.0, "bracket", cullface=["down"]))
    return elements

pole_textures = {
    "particle": "powergrid:block/treated_wood_pole",
    "pole": "powergrid:block/treated_wood_pole",
    "end": "powergrid:block/treated_wood_pole_end",
    "bracket": "powergrid:block/steel_bracket"
}

write_json(os.path.join(MODELS_BLOCK, "treated_wood_pole.json"), {
    "textures": pole_textures,
    "elements": build_pole_elements(False, False)
})
write_json(os.path.join(MODELS_BLOCK, "treated_wood_pole_top.json"), {
    "textures": pole_textures,
    "elements": build_pole_elements(True, False)
})
write_json(os.path.join(MODELS_BLOCK, "treated_wood_pole_bottom.json"), {
    "textures": pole_textures,
    "elements": build_pole_elements(False, True)
})
write_json(os.path.join(MODELS_BLOCK, "treated_wood_pole_single.json"), {
    "textures": pole_textures,
    "elements": build_pole_elements(True, True)
})

# 2. Power Pole Crossarm Models
crossarm_textures = {
    "particle": "powergrid:block/pole_crossarm",
    "wood": "powergrid:block/pole_crossarm",
    "end": "powergrid:block/pole_crossarm_end",
    "bracket": "powergrid:block/steel_bracket"
}

write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_beam_x.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(0.0, 10.5, 5.0, 16.0, 15.5, 11.0, "wood"),
        make_element(0.0, 10.0, 5.5, 16.0, 16.0, 10.5, "wood"),
        make_element(7.2, 9.8, 4.8, 8.8, 16.2, 11.2, "bracket")
    ]
})

write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_beam_z.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(5.0, 10.5, 0.0, 11.0, 15.5, 16.0, "wood"),
        make_element(5.5, 10.0, 0.0, 10.5, 16.0, 16.0, "wood"),
        make_element(4.8, 9.8, 7.2, 11.2, 16.2, 8.8, "bracket")
    ]
})

write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_bracket_neg_x.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(0.0, 9.0, 4.5, 2.0, 16.5, 11.5, "bracket"),
        make_element(-2.0, 10.0, 4.2, 0.0, 16.0, 11.8, "bracket"),
        make_element(2.0, 12.0, 5.0, 2.6, 13.5, 6.0, "bracket"),
        make_element(2.0, 12.0, 10.0, 2.6, 13.5, 11.0, "bracket")
    ]
})
write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_bracket_pos_x.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(14.0, 9.0, 4.5, 16.0, 16.5, 11.5, "bracket"),
        make_element(16.0, 10.0, 4.2, 18.0, 16.0, 11.8, "bracket"),
        make_element(13.4, 12.0, 5.0, 14.0, 13.5, 6.0, "bracket"),
        make_element(13.4, 12.0, 10.0, 14.0, 13.5, 11.0, "bracket")
    ]
})
write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_bracket_neg_z.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(4.5, 9.0, 0.0, 11.5, 16.5, 2.0, "bracket"),
        make_element(4.2, 10.0, -2.0, 11.8, 16.0, 0.0, "bracket"),
        make_element(5.0, 12.0, 2.0, 6.0, 13.5, 2.6, "bracket"),
        make_element(10.0, 12.0, 2.0, 11.0, 13.5, 2.6, "bracket")
    ]
})
write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_bracket_pos_z.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(4.5, 9.0, 14.0, 11.5, 16.5, 16.0, "bracket"),
        make_element(4.2, 10.0, 16.0, 11.8, 16.0, 18.0, "bracket"),
        make_element(5.0, 12.0, 13.4, 6.0, 13.5, 14.0, "bracket"),
        make_element(10.0, 12.0, 13.4, 11.0, 13.5, 14.0, "bracket")
    ]
})

write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_end_neg_x.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(0.0, 9.8, 4.8, 1.2, 16.2, 11.2, "bracket"),
        make_element(2.0, 16.0, 7.2, 3.5, 17.5, 8.8, "bracket")
    ]
})
write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_end_pos_x.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(14.8, 9.8, 4.8, 16.0, 16.2, 11.2, "bracket"),
        make_element(12.5, 16.0, 7.2, 14.0, 17.5, 8.8, "bracket")
    ]
})
write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_end_neg_z.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(4.8, 9.8, 0.0, 11.2, 16.2, 1.2, "bracket"),
        make_element(7.2, 16.0, 2.0, 8.8, 17.5, 3.5, "bracket")
    ]
})
write_json(os.path.join(MODELS_BLOCK, "pole_crossarm_end_pos_z.json"), {
    "textures": crossarm_textures,
    "elements": [
        make_element(4.8, 9.8, 14.8, 11.2, 16.2, 16.0, "bracket"),
        make_element(7.2, 16.0, 12.5, 8.8, 17.5, 14.0, "bracket")
    ]
})

# 3. Steel Truss Pole Model
write_json(os.path.join(MODELS_BLOCK, "steel_truss_pole.json"), {
    "textures": {
        "particle": "powergrid:block/steel_truss_pole",
        "truss": "powergrid:block/steel_truss_pole",
        "bracket": "powergrid:block/steel_bracket"
    },
    "elements": [
        make_element(4.0, 0.0, 4.0, 5.5, 16.0, 5.5, "bracket"),
        make_element(10.5, 0.0, 4.0, 12.0, 16.0, 5.5, "bracket"),
        make_element(4.0, 0.0, 10.5, 5.5, 16.0, 12.0, "bracket"),
        make_element(10.5, 0.0, 10.5, 12.0, 16.0, 12.0, "bracket"),
        make_element(5.0, 0.0, 5.0, 11.0, 16.0, 11.0, "truss")
    ]
})

# 4. Wire Relay 3D Models
def build_relay_model(tier, insul_tex, clamp_tex):
    if tier == "lv":
        elements = [
            make_element(5.0, 0.0, 5.0, 11.0, 1.0, 11.0, "bracket", cullface=["down"]),
            make_element(7.0, 1.0, 7.0, 9.0, 2.5, 9.0, "bracket"),
            make_element(5.0, 2.5, 5.0, 11.0, 4.5, 11.0, "insulator"),
            make_element(6.0, 4.5, 6.0, 10.0, 6.0, 10.0, "insulator"),
            make_element(5.5, 6.0, 6.5, 10.5, 7.5, 9.5, "clamp"),
            make_element(7.5, 7.5, 7.5, 8.5, 8.2, 8.5, "clamp")
        ]
    elif tier == "mv":
        elements = [
            make_element(4.5, 0.0, 4.5, 11.5, 1.0, 11.5, "bracket", cullface=["down"]),
            make_element(7.0, 1.0, 7.0, 9.0, 2.0, 9.0, "bracket"),
            make_element(4.0, 2.0, 4.0, 12.0, 4.0, 12.0, "insulator"),
            make_element(6.0, 4.0, 6.0, 10.0, 4.8, 10.0, "bracket"),
            make_element(5.0, 4.8, 5.0, 11.0, 6.8, 11.0, "insulator"),
            make_element(6.0, 6.8, 6.0, 10.0, 8.0, 10.0, "insulator"),
            make_element(5.0, 8.0, 6.0, 11.0, 9.5, 10.0, "clamp"),
            make_element(6.5, 9.5, 7.5, 9.5, 10.2, 8.5, "clamp")
        ]
    else: # hv
        elements = [
            make_element(4.0, 0.0, 4.0, 12.0, 1.2, 12.0, "bracket", cullface=["down"]),
            make_element(3.5, 1.2, 3.5, 12.5, 3.5, 12.5, "insulator"),
            make_element(6.0, 3.5, 6.0, 10.0, 4.2, 10.0, "bracket"),
            make_element(4.5, 4.2, 4.5, 11.5, 6.5, 11.5, "insulator"),
            make_element(6.0, 6.5, 6.0, 10.0, 7.2, 10.0, "bracket"),
            make_element(5.0, 7.2, 5.0, 11.0, 9.5, 11.0, "insulator"),
            make_element(4.5, 9.5, 5.5, 11.5, 11.5, 10.5, "clamp"),
            make_element(5.5, 11.5, 6.5, 10.5, 12.5, 9.5, "clamp")
        ]
    return {
        "textures": {
            "particle": f"powergrid:block/{insul_tex}",
            "bracket": "powergrid:block/relay_bracket",
            "insulator": f"powergrid:block/{insul_tex}",
            "clamp": f"powergrid:block/{clamp_tex}"
        },
        "elements": elements
    }

write_json(os.path.join(MODELS_BLOCK, "lv_wire_relay.json"), build_relay_model("lv", "relay_insulator_lv", "wire_clamp_lv"))
write_json(os.path.join(MODELS_BLOCK, "mv_wire_relay.json"), build_relay_model("mv", "relay_insulator_mv", "wire_clamp_mv"))
write_json(os.path.join(MODELS_BLOCK, "hv_wire_relay.json"), build_relay_model("hv", "relay_insulator_hv", "wire_clamp_hv"))

# 5. Wire Connector 3D Models
def build_connector_model(tier, insul_tex):
    if tier == "lv":
        elements = [
            make_element(4.0, 0.0, 4.0, 12.0, 1.5, 12.0, "base", cullface=["down"]),
            make_element(5.5, 1.5, 5.5, 10.5, 3.0, 10.5, "base"),
            make_element(5.0, 3.0, 5.0, 11.0, 6.0, 11.0, "bushing"),
            make_element(6.0, 6.0, 6.0, 10.0, 8.0, 10.0, "terminal"),
            make_element(7.0, 8.0, 7.0, 9.0, 9.0, 9.0, "terminal")
        ]
    elif tier == "mv":
        elements = [
            make_element(3.5, 0.0, 3.5, 12.5, 1.5, 12.5, "base", cullface=["down"]),
            make_element(5.0, 1.5, 5.0, 11.0, 3.2, 11.0, "base"),
            make_element(4.5, 3.2, 4.5, 11.5, 5.5, 11.5, "bushing"),
            make_element(5.0, 5.5, 5.0, 11.0, 7.5, 11.0, "bushing"),
            make_element(5.5, 7.5, 5.5, 10.5, 9.5, 10.5, "terminal"),
            make_element(6.5, 9.5, 6.5, 9.5, 10.5, 9.5, "terminal")
        ]
    else: # hv
        elements = [
            make_element(3.0, 0.0, 3.0, 13.0, 1.8, 13.0, "base", cullface=["down"]),
            make_element(4.5, 1.8, 4.5, 11.5, 3.8, 11.5, "base"),
            make_element(4.0, 3.8, 4.0, 12.0, 8.5, 12.0, "bushing"),
            make_element(5.0, 8.5, 5.0, 11.0, 10.0, 11.0, "base"),
            make_element(5.0, 10.0, 5.0, 11.0, 12.0, 11.0, "terminal"),
            make_element(4.5, 12.0, 4.5, 11.5, 13.0, 11.5, "terminal")
        ]
    return {
        "textures": {
            "particle": "powergrid:block/connector_base",
            "base": "powergrid:block/connector_base",
            "bushing": f"powergrid:block/{insul_tex}",
            "terminal": "powergrid:block/connector_terminal"
        },
        "elements": elements
    }

write_json(os.path.join(MODELS_BLOCK, "lv_wire_connector.json"), build_connector_model("lv", "connector_bushing_lv"))
write_json(os.path.join(MODELS_BLOCK, "mv_wire_connector.json"), build_connector_model("mv", "connector_bushing_mv"))
write_json(os.path.join(MODELS_BLOCK, "hv_wire_connector.json"), build_connector_model("hv", "connector_bushing_hv"))

# 6. Industrial Blocks Models
standard_cubes = [
    ("generator_casing", "generator_casing"),
    ("alternator_coil", "alternator_coil"),
    ("turbine_rotor", "turbine_rotor")
]
for name, tex in standard_cubes:
    write_json(os.path.join(MODELS_BLOCK, f"{name}.json"), {
        "parent": "minecraft:block/cube_all",
        "textures": {"all": f"powergrid:block/{tex}"}
    })

# 7. Breaker Switch (Heavy Duty Safety Switch / Disconnect Enclosure) 3D Models
def build_breaker_switch_model(is_on):
    front_tex = "breaker_box_front_on" if is_on else "breaker_box_front_off"
    elements = [
        # Back wall mounting flange
        make_element(3.0, 1.5, 15.5, 13.0, 15.5, 16.0, "side", cullface=["south"]),
        # Main steel enclosure body
        make_element(3.5, 2.5, 11.5, 12.5, 14.5, 15.5, "side"),
        # Front beveled door
        make_element(3.2, 2.2, 10.5, 12.8, 14.8, 11.5, "side"),
        # Embossed center door face with Siemens-style placard
        make_element(4.2, 3.2, 10.2, 11.8, 13.8, 10.5, "front"),
        # Top conduit entrance hub
        make_element(6.5, 14.5, 12.5, 9.5, 16.0, 14.5, "top", cullface=["up"]),
        # Bottom conduit exit hub
        make_element(6.5, 1.0, 12.5, 9.5, 2.5, 14.5, "top", cullface=["down"]),
        # Padlockable door latch clasp
        make_element(12.0, 3.5, 10.0, 13.0, 5.0, 10.8, "side"),
        # Side pivot bracket on right side
        make_element(12.8, 6.5, 12.5, 13.6, 9.5, 14.5, "side")
    ]

    if is_on:
        # Lever pointing UP (Circuit Closed / Power ON)
        elements.append(make_element(13.4, 8.0, 12.8, 14.2, 12.5, 13.8, "handle"))
        # Molded bright RED safety grip handle (UP position)
        elements.append(make_element(13.0, 11.5, 11.5, 14.6, 15.5, 14.2, "handle"))
    else:
        # Lever swung DOWN (Circuit Open / Power OFF)
        elements.append(make_element(13.4, 4.5, 12.8, 14.2, 9.0, 13.8, "handle"))
        # Molded bright RED safety grip handle (DOWN position)
        elements.append(make_element(13.0, 1.5, 11.5, 14.6, 5.5, 14.2, "handle"))

    return {
        "textures": {
            "particle": "powergrid:block/breaker_box_side",
            "front": f"powergrid:block/{front_tex}",
            "side": "powergrid:block/breaker_box_side",
            "top": "powergrid:block/breaker_box_top",
            "handle": "powergrid:block/breaker_handle"
        },
        "elements": elements
    }

write_json(os.path.join(MODELS_BLOCK, "breaker_switch_on.json"), build_breaker_switch_model(True))
write_json(os.path.join(MODELS_BLOCK, "breaker_switch_off.json"), build_breaker_switch_model(False))

# 8. Transformer & Machines
write_json(os.path.join(MODELS_BLOCK, "transformer.json"), {
    "parent": "minecraft:block/cube_directional",
    "textures": {
        "particle": "powergrid:block/transformer_side",
        "up": "powergrid:block/transformer_top",
        "down": "powergrid:block/transformer_top",
        "north": "powergrid:block/transformer_front",
        "south": "powergrid:block/transformer_side",
        "east": "powergrid:block/transformer_side",
        "west": "powergrid:block/transformer_side"
    }
})

write_json(os.path.join(MODELS_BLOCK, "heavy_accumulator.json"), {
    "parent": "minecraft:block/cube_column",
    "textures": {
        "end": "powergrid:block/heavy_accumulator_top",
        "side": "powergrid:block/heavy_accumulator_side"
    }
})

write_json(os.path.join(MODELS_BLOCK, "exhaust_stack.json"), {
    "parent": "minecraft:block/cube_column",
    "textures": {
        "end": "powergrid:block/exhaust_stack_top",
        "side": "powergrid:block/exhaust_stack_side"
    }
})

write_json(os.path.join(MODELS_BLOCK, "power_plant_controller.json"), {
    "parent": "minecraft:block/cube_directional",
    "textures": {
        "particle": "powergrid:block/generator_casing",
        "up": "powergrid:block/generator_casing",
        "down": "powergrid:block/generator_casing",
        "north": "powergrid:block/power_plant_controller",
        "south": "powergrid:block/generator_casing",
        "east": "powergrid:block/generator_casing",
        "west": "powergrid:block/generator_casing"
    }
})

# ==============================================================================
# BLOCKSTATES
# ==============================================================================

write_json(os.path.join(BLOCKSTATES, "treated_wood_pole.json"), {
    "variants": {
        "down=false,up=false": {"model": "powergrid:block/treated_wood_pole_single"},
        "down=false,up=true":  {"model": "powergrid:block/treated_wood_pole_bottom"},
        "down=true,up=false":   {"model": "powergrid:block/treated_wood_pole_top"},
        "down=true,up=true":    {"model": "powergrid:block/treated_wood_pole"}
    }
})

write_json(os.path.join(BLOCKSTATES, "pole_crossarm.json"), {
    "multipart": [
        {"when": {"axis": "x"}, "apply": {"model": "powergrid:block/pole_crossarm_beam_x"}},
        {"when": {"axis": "z"}, "apply": {"model": "powergrid:block/pole_crossarm_beam_z"}},
        {"when": {"axis": "x", "neg_pole": "true"}, "apply": {"model": "powergrid:block/pole_crossarm_bracket_neg_x"}},
        {"when": {"axis": "x", "pos_pole": "true"}, "apply": {"model": "powergrid:block/pole_crossarm_bracket_pos_x"}},
        {"when": {"axis": "z", "neg_pole": "true"}, "apply": {"model": "powergrid:block/pole_crossarm_bracket_neg_z"}},
        {"when": {"axis": "z", "pos_pole": "true"}, "apply": {"model": "powergrid:block/pole_crossarm_bracket_pos_z"}},
        {"when": {"axis": "x", "neg_end": "true"}, "apply": {"model": "powergrid:block/pole_crossarm_end_neg_x"}},
        {"when": {"axis": "x", "pos_end": "true"}, "apply": {"model": "powergrid:block/pole_crossarm_end_pos_x"}},
        {"when": {"axis": "z", "neg_end": "true"}, "apply": {"model": "powergrid:block/pole_crossarm_end_neg_z"}},
        {"when": {"axis": "z", "pos_end": "true"}, "apply": {"model": "powergrid:block/pole_crossarm_end_pos_z"}}
    ]
})

write_json(os.path.join(BLOCKSTATES, "steel_truss_pole.json"), {
    "variants": {"": {"model": "powergrid:block/steel_truss_pole"}}
})

# Breaker Switch Blockstate (Directional & Open/Closed States)
write_json(os.path.join(BLOCKSTATES, "breaker_switch.json"), {
    "variants": {
        "facing=north,open=false,powered=false": {"model": "powergrid:block/breaker_switch_on"},
        "facing=north,open=false,powered=true":  {"model": "powergrid:block/breaker_switch_on"},
        "facing=north,open=true,powered=false":  {"model": "powergrid:block/breaker_switch_off"},
        "facing=north,open=true,powered=true":   {"model": "powergrid:block/breaker_switch_off"},

        "facing=east,open=false,powered=false":  {"model": "powergrid:block/breaker_switch_on", "y": 90},
        "facing=east,open=false,powered=true":   {"model": "powergrid:block/breaker_switch_on", "y": 90},
        "facing=east,open=true,powered=false":   {"model": "powergrid:block/breaker_switch_off", "y": 90},
        "facing=east,open=true,powered=true":    {"model": "powergrid:block/breaker_switch_off", "y": 90},

        "facing=south,open=false,powered=false": {"model": "powergrid:block/breaker_switch_on", "y": 180},
        "facing=south,open=false,powered=true":  {"model": "powergrid:block/breaker_switch_on", "y": 180},
        "facing=south,open=true,powered=false":  {"model": "powergrid:block/breaker_switch_off", "y": 180},
        "facing=south,open=true,powered=true":   {"model": "powergrid:block/breaker_switch_off", "y": 180},

        "facing=west,open=false,powered=false":  {"model": "powergrid:block/breaker_switch_on", "y": 270},
        "facing=west,open=false,powered=true":   {"model": "powergrid:block/breaker_switch_on", "y": 270},
        "facing=west,open=true,powered=false":   {"model": "powergrid:block/breaker_switch_off", "y": 270},
        "facing=west,open=true,powered=true":    {"model": "powergrid:block/breaker_switch_off", "y": 270}
    }
})

directional_devices = [
    "lv_wire_relay", "mv_wire_relay", "hv_wire_relay",
    "lv_wire_connector", "mv_wire_connector", "hv_wire_connector"
]
for dev in directional_devices:
    write_json(os.path.join(BLOCKSTATES, f"{dev}.json"), {
        "variants": {
            "facing=down":  {"model": f"powergrid:block/{dev}", "x": 180},
            "facing=up":    {"model": f"powergrid:block/{dev}"},
            "facing=north": {"model": f"powergrid:block/{dev}", "x": 90},
            "facing=south": {"model": f"powergrid:block/{dev}", "x": 90, "y": 180},
            "facing=west":  {"model": f"powergrid:block/{dev}", "x": 90, "y": 270},
            "facing=east":  {"model": f"powergrid:block/{dev}", "x": 90, "y": 90}
        }
    })

write_json(os.path.join(BLOCKSTATES, "transformer.json"), {
    "variants": {
        "facing=north": {"model": "powergrid:block/transformer"},
        "facing=east":  {"model": "powergrid:block/transformer", "y": 90},
        "facing=south": {"model": "powergrid:block/transformer", "y": 180},
        "facing=west":  {"model": "powergrid:block/transformer", "y": 270}
    }
})

for b in ["heavy_accumulator", "generator_casing", "alternator_coil", "turbine_rotor", "exhaust_stack"]:
    write_json(os.path.join(BLOCKSTATES, f"{b}.json"), {
        "variants": {"": {"model": f"powergrid:block/{b}"}}
    })

write_json(os.path.join(BLOCKSTATES, "power_plant_controller.json"), {
    "variants": {
        "facing=north": {"model": "powergrid:block/power_plant_controller"},
        "facing=east":  {"model": "powergrid:block/power_plant_controller", "y": 90},
        "facing=south": {"model": "powergrid:block/power_plant_controller", "y": 180},
        "facing=west":  {"model": "powergrid:block/power_plant_controller", "y": 270}
    }
})

# ==============================================================================
# ITEM MODELS & ITEM DEFINITIONS (Modern 26.3 Specification)
# ==============================================================================

ALL_BLOCKS = [
    "treated_wood_pole", "steel_truss_pole", "pole_crossarm",
    "lv_wire_connector", "mv_wire_connector", "hv_wire_connector",
    "lv_wire_relay", "mv_wire_relay", "hv_wire_relay",
    "transformer", "breaker_switch", "heavy_accumulator",
    "power_plant_controller", "generator_casing", "alternator_coil",
    "turbine_rotor", "exhaust_stack"
]

ALL_ITEMS = [
    "engineer_hammer", "voltmeter", "wire_cutters",
    "lv_wire_coil", "mv_wire_coil", "hv_wire_coil",
    "treated_wood", "copper_wire", "electrum_wire", "steel_strand", "ceramic_insulator"
]

display_transform = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]}
}

for b in ALL_BLOCKS:
    target_model = f"powergrid:block/{b}"
    if b == "treated_wood_pole":
        target_model = "powergrid:block/treated_wood_pole_single"
    elif b == "pole_crossarm":
        target_model = "powergrid:block/pole_crossarm_beam_x"
    elif b == "breaker_switch":
        target_model = "powergrid:block/breaker_switch_on"

    write_json(os.path.join(MODELS_ITEM, f"{b}.json"), {
        "parent": target_model,
        "display": display_transform
    })
    write_json(os.path.join(ITEM_DEFS, f"{b}.json"), {
        "model": {
            "type": "minecraft:model",
            "model": f"powergrid:item/{b}"
        }
    })

for it in ALL_ITEMS:
    write_json(os.path.join(MODELS_ITEM, f"{it}.json"), {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"powergrid:item/{it}"
        }
    })
    write_json(os.path.join(ITEM_DEFS, f"{it}.json"), {
        "model": {
            "type": "minecraft:model",
            "model": f"powergrid:item/{it}"
        }
    })

print("Asset generation complete! All 32x32 textures, 1024x1024 resource pack, 3D models, blockstates, and item definitions created.")
