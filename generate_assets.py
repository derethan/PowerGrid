import os
import json
from PIL import Image, ImageDraw

BASE_DIR = r"C:\Users\andg_\OneDrive\Desktop\minecraft\PowerGrid\src\main\resources\assets\powergrid"

BLOCKS_DIR = os.path.join(BASE_DIR, "textures", "block")
ITEMS_DIR = os.path.join(BASE_DIR, "textures", "item")
MODELS_BLOCK = os.path.join(BASE_DIR, "models", "block")
MODELS_ITEM = os.path.join(BASE_DIR, "models", "item")
BLOCKSTATES = os.path.join(BASE_DIR, "blockstates")
ITEM_DEFS = os.path.join(BASE_DIR, "items")

for d in [BLOCKS_DIR, ITEMS_DIR, MODELS_BLOCK, MODELS_ITEM, BLOCKSTATES, ITEM_DEFS]:
    os.makedirs(d, exist_ok=True)

def write_json(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

def new_img():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))

# --- TEXTURE GENERATION HELPERS ---

def make_treated_wood_pole():
    im = Image.new("RGBA", (16, 16), (45, 30, 20, 255))
    d = ImageDraw.Draw(im)
    # Wood grain
    for y in range(16):
        c = (55, 38, 25) if y % 3 == 0 else ((40, 26, 17) if y % 3 == 1 else (48, 33, 22))
        d.line([(0, y), (15, y)], fill=c)
    # Dark creosote stains
    d.line([(3, 0), (3, 15)], fill=(32, 20, 12))
    d.line([(12, 0), (12, 15)], fill=(35, 22, 14))
    # Metal reinforcing bands at y=3 and y=12
    for by in [3, 12]:
        d.line([(0, by), (15, by)], fill=(70, 75, 80))
        d.line([(0, by+1), (15, by+1)], fill=(50, 55, 60))
        d.point([(2, by), (8, by), (13, by)], fill=(120, 125, 130))
    im.save(os.path.join(BLOCKS_DIR, "treated_wood_pole.png"))

def make_steel_truss_pole():
    im = Image.new("RGBA", (16, 16), (30, 32, 36, 255))
    d = ImageDraw.Draw(im)
    # Outer steel beams
    d.rectangle([0, 0, 15, 15], outline=(65, 70, 78))
    d.rectangle([1, 1, 14, 14], outline=(45, 48, 54))
    # Diagonal truss cross-struts
    d.line([(2, 2), (13, 13)], fill=(75, 82, 92), width=1)
    d.line([(2, 13), (13, 2)], fill=(75, 82, 92), width=1)
    d.line([(3, 2), (14, 13)], fill=(45, 48, 54), width=1)
    # Center junction plate with rivet
    d.rectangle([6, 6, 9, 9], fill=(60, 65, 72))
    d.point([(7, 7), (8, 8)], fill=(120, 130, 140))
    # Corner rivets
    for rx, ry in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        d.point([(rx, ry)], fill=(130, 140, 155))
    im.save(os.path.join(BLOCKS_DIR, "steel_truss_pole.png"))

def make_pole_crossarm():
    im = Image.new("RGBA", (16, 16), (48, 33, 22, 255))
    d = ImageDraw.Draw(im)
    # Horizontal wood grain
    for x in range(16):
        c = (56, 39, 26) if x % 4 == 0 else (44, 29, 19)
        d.line([(x, 0), (x, 15)], fill=c)
    # Steel end-caps and mounting brackets
    d.rectangle([0, 0, 2, 15], fill=(60, 65, 72))
    d.rectangle([13, 0, 15, 15], fill=(60, 65, 72))
    d.rectangle([6, 4, 9, 11], fill=(50, 55, 62)) # center bracket
    d.point([(1, 4), (1, 11), (14, 4), (14, 11), (7, 7), (8, 8)], fill=(130, 140, 155))
    im.save(os.path.join(BLOCKS_DIR, "pole_crossarm.png"))

def make_connector(name, plate_col, insul_col, highlight_col):
    im = Image.new("RGBA", (16, 16), (35, 38, 42, 255))
    d = ImageDraw.Draw(im)
    # Heavy base plate
    d.rectangle([0, 0, 15, 15], outline=(55, 60, 66))
    d.rectangle([2, 2, 13, 13], fill=(28, 30, 34))
    # Ceramic insulator ribs
    d.rectangle([4, 4, 11, 11], fill=insul_col)
    d.rectangle([5, 5, 10, 10], outline=(insul_col[0]-30, insul_col[1]-30, insul_col[2]-30))
    # Metallic terminal core
    d.rectangle([6, 6, 9, 9], fill=plate_col)
    d.point([(7, 7), (8, 7)], fill=highlight_col)
    # Corner mounting bolts
    for rx, ry in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        d.point([(rx, ry)], fill=(120, 130, 145))
    im.save(os.path.join(BLOCKS_DIR, f"{name}.png"))

def make_relay(name, insul_col, tip_col):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    # Standoff bracket
    d.rectangle([4, 11, 11, 15], fill=(45, 48, 54))
    d.line([(4, 11), (11, 11)], fill=(75, 80, 90))
    # Ribbed insulator bell
    d.rectangle([3, 5, 12, 10], fill=insul_col)
    d.line([(2, 7), (13, 7)], fill=(insul_col[0]+25, insul_col[1]+25, insul_col[2]+25))
    d.line([(3, 9), (12, 9)], fill=(insul_col[0]-30, insul_col[1]-30, insul_col[2]-30))
    # Top wire saddle clamp
    d.rectangle([5, 1, 10, 4], fill=tip_col)
    d.point([(7, 2), (8, 2)], fill=(255, 255, 255))
    im.save(os.path.join(BLOCKS_DIR, f"{name}.png"))

def make_transformer():
    # Transformer Side
    im = Image.new("RGBA", (16, 16), (42, 45, 50, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], outline=(65, 70, 78))
    # Vertical oil cooling radiator fins
    for x in range(2, 14, 2):
        d.line([(x, 2), (x, 13)], fill=(30, 32, 36))
        d.line([(x+1, 2), (x+1, 13)], fill=(55, 60, 68))
    im.save(os.path.join(BLOCKS_DIR, "transformer_side.png"))

    # Transformer Top
    im2 = Image.new("RGBA", (16, 16), (48, 52, 58, 255))
    d2 = ImageDraw.Draw(im2)
    d2.rectangle([0, 0, 15, 15], outline=(70, 76, 85))
    # Bushing mount terminals (LV side & HV side)
    d2.rectangle([2, 4, 5, 11], fill=(180, 110, 50)) # copper LV
    d2.rectangle([10, 3, 14, 12], fill=(210, 215, 220)) # ceramic HV
    d2.point([(3, 7), (4, 8)], fill=(255, 180, 80))
    d2.point([(12, 7), (12, 8)], fill=(255, 255, 255))
    im2.save(os.path.join(BLOCKS_DIR, "transformer_top.png"))

    # Transformer Front
    im3 = im.copy()
    d3 = ImageDraw.Draw(im3)
    # Rating plaque & oil level sight glass
    d3.rectangle([4, 4, 11, 9], fill=(160, 140, 70)) # brass plate
    d3.rectangle([5, 5, 10, 8], fill=(120, 100, 45))
    d3.line([(7, 11), (7, 14)], fill=(100, 180, 220)) # sight glass
    im3.save(os.path.join(BLOCKS_DIR, "transformer_front.png"))

def make_breaker_switch():
    im = Image.new("RGBA", (16, 16), (36, 38, 42, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], outline=(60, 65, 72))
    # Slate backing plate
    d.rectangle([2, 2, 13, 13], fill=(24, 26, 30))
    # Copper knife blade (closed)
    d.rectangle([6, 3, 9, 12], fill=(190, 100, 45))
    d.line([(7, 4), (7, 11)], fill=(235, 140, 75))
    d.point([(7, 3), (8, 3), (7, 12), (8, 12)], fill=(255, 180, 90))
    # Insulated handle
    d.rectangle([5, 1, 10, 3], fill=(160, 30, 30))
    im.save(os.path.join(BLOCKS_DIR, "breaker_switch.png"))

def make_heavy_accumulator():
    # Side
    im = Image.new("RGBA", (16, 16), (34, 37, 42, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], outline=(55, 60, 68))
    # Heavy battery cell slits
    d.rectangle([2, 3, 13, 6], fill=(22, 24, 28))
    d.rectangle([2, 9, 13, 12], fill=(22, 24, 28))
    # Glowing cyan charge LED bar
    d.rectangle([3, 7, 12, 8], fill=(20, 120, 140))
    d.line([(4, 7), (10, 7)], fill=(60, 220, 255))
    im.save(os.path.join(BLOCKS_DIR, "heavy_accumulator_side.png"))

    # Top
    im2 = Image.new("RGBA", (16, 16), (40, 44, 50, 255))
    d2 = ImageDraw.Draw(im2)
    d2.rectangle([0, 0, 15, 15], outline=(65, 72, 82))
    # Positive (+) terminal brass, Negative (-) terminal steel
    d2.rectangle([2, 5, 6, 10], fill=(190, 130, 40)) # + brass
    d2.point([(4, 7), (4, 8)], fill=(255, 200, 80))
    d2.rectangle([9, 5, 13, 10], fill=(100, 110, 125)) # - steel
    d2.point([(11, 7), (11, 8)], fill=(180, 195, 215))
    im2.save(os.path.join(BLOCKS_DIR, "heavy_accumulator_top.png"))

def make_generator_casing():
    im = Image.new("RGBA", (16, 16), (42, 46, 52, 255))
    d = ImageDraw.Draw(im)
    # Beveled heavy steel frame
    d.rectangle([0, 0, 15, 15], outline=(68, 75, 85))
    d.rectangle([1, 1, 14, 14], outline=(30, 33, 38))
    d.rectangle([2, 2, 13, 13], fill=(38, 42, 48))
    # Corner rivets
    for rx, ry in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        d.point([(rx, ry)], fill=(135, 145, 160))
    # Center seam
    d.line([(2, 8), (13, 8)], fill=(26, 29, 33))
    d.line([(2, 9), (13, 9)], fill=(52, 58, 66))
    im.save(os.path.join(BLOCKS_DIR, "generator_casing.png"))

def make_alternator_coil():
    im = Image.new("RGBA", (16, 16), (35, 38, 44, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], outline=(55, 60, 68))
    # Dense copper winding grooves
    for y in range(2, 14, 2):
        d.line([(2, y), (13, y)], fill=(210, 115, 50))
        d.line([(2, y+1), (13, y+1)], fill=(160, 80, 35))
        d.point([(4, y), (8, y), (11, y)], fill=(255, 175, 95))
    im.save(os.path.join(BLOCKS_DIR, "alternator_coil.png"))

def make_turbine_rotor():
    im = Image.new("RGBA", (16, 16), (32, 35, 40, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], outline=(58, 64, 72))
    # Circular shroud
    d.ellipse([2, 2, 13, 13], outline=(75, 82, 95))
    d.ellipse([3, 3, 12, 12], fill=(20, 22, 26))
    # Curved rotor blades
    d.line([(4, 4), (11, 11)], fill=(120, 130, 145))
    d.line([(4, 11), (11, 4)], fill=(120, 130, 145))
    d.line([(7, 3), (7, 12)], fill=(140, 150, 165))
    d.line([(3, 7), (12, 7)], fill=(140, 150, 165))
    # Center hub spinner
    d.ellipse([6, 6, 9, 9], fill=(180, 190, 205))
    d.point([(7, 7)], fill=(255, 255, 255))
    im.save(os.path.join(BLOCKS_DIR, "turbine_rotor.png"))

def make_exhaust_stack():
    # Side
    im = Image.new("RGBA", (16, 16), (36, 39, 44, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], outline=(55, 60, 68))
    # Vertical flue ridges
    for x in range(3, 14, 3):
        d.line([(x, 0), (x, 15)], fill=(28, 30, 34))
        d.line([(x+1, 0), (x+1, 15)], fill=(58, 64, 72))
    # Reinforcing collar at top & bottom
    d.line([(0, 2), (15, 2)], fill=(80, 88, 100))
    d.line([(0, 13), (15, 13)], fill=(80, 88, 100))
    im.save(os.path.join(BLOCKS_DIR, "exhaust_stack_side.png"))

    # Top (dark vent grille)
    im2 = Image.new("RGBA", (16, 16), (38, 42, 48, 255))
    d2 = ImageDraw.Draw(im2)
    d2.rectangle([0, 0, 15, 15], outline=(65, 72, 82))
    d2.ellipse([2, 2, 13, 13], fill=(12, 14, 16))
    d2.line([(4, 7), (11, 7)], fill=(40, 45, 52))
    d2.line([(7, 4), (7, 11)], fill=(40, 45, 52))
    im2.save(os.path.join(BLOCKS_DIR, "exhaust_stack_top.png"))

def make_power_plant_controller():
    im = Image.new("RGBA", (16, 16), (42, 46, 52, 255))
    d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], outline=(68, 75, 85))
    d.rectangle([1, 1, 14, 14], fill=(32, 35, 40))
    # Round analog gauge (needle dial)
    d.ellipse([3, 3, 8, 8], fill=(225, 220, 200), outline=(85, 90, 100))
    d.line([(5, 6), (7, 4)], fill=(180, 30, 30)) # needle
    # Amber/Green status lights
    d.ellipse([11, 3, 13, 5], fill=(70, 220, 80)) # green running
    d.ellipse([11, 7, 13, 9], fill=(230, 160, 30)) # amber ready
    # Emergency Stop switch & breaker handle
    d.rectangle([3, 11, 6, 13], fill=(200, 35, 35)) # red E-stop
    d.rectangle([8, 11, 13, 13], fill=(55, 60, 68))
    d.line([(10, 10), (12, 10)], fill=(190, 130, 50))
    im.save(os.path.join(BLOCKS_DIR, "power_plant_controller.png"))

# --- ITEM TEXTURE GENERATION ---

def make_engineer_hammer():
    im = new_img()
    d = ImageDraw.Draw(im)
    # Wooden handle diagonally from (2,13) to (9,6)
    for i in range(8):
        d.point([(2+i, 13-i)], fill=(110, 70, 40))
        d.point([(3+i, 13-i)], fill=(140, 95, 55))
    # Brass ferrule
    d.point([(9, 6), (10, 5)], fill=(200, 150, 50))
    # Heavy forged hammer head (10, 2) to (14, 6)
    d.rectangle([9, 2, 14, 5], fill=(60, 65, 75))
    d.rectangle([8, 1, 15, 4], fill=(85, 95, 110))
    d.line([(9, 1), (14, 1)], fill=(140, 155, 175))
    d.line([(8, 4), (15, 4)], fill=(40, 45, 52))
    im.save(os.path.join(ITEMS_DIR, "engineer_hammer.png"))

def make_voltmeter():
    im = new_img()
    d = ImageDraw.Draw(im)
    # Brass circular meter body
    d.ellipse([2, 3, 13, 14], fill=(180, 140, 50), outline=(220, 185, 80))
    d.ellipse([4, 5, 11, 12], fill=(235, 230, 215))
    # Dial markings & needle
    d.arc([5, 6, 10, 11], start=180, end=0, fill=(70, 70, 70))
    d.line([(7, 10), (9, 7)], fill=(190, 30, 30))
    # Probe leads (Red & Black) extending upwards
    d.line([(5, 3), (3, 0)], fill=(200, 40, 40))
    d.line([(10, 3), (12, 0)], fill=(30, 30, 30))
    d.point([(3, 0), (12, 0)], fill=(210, 215, 225)) # metal tips
    im.save(os.path.join(ITEMS_DIR, "voltmeter.png"))

def make_wire_cutters():
    im = new_img()
    d = ImageDraw.Draw(im)
    # Insulated red/yellow rubber handles
    for i in range(7):
        d.point([(2+i, 14-i)], fill=(210, 40, 40))
        d.point([(1+i, 14-i)], fill=(240, 80, 80))
        d.point([(13-i, 14-i)], fill=(210, 40, 40))
        d.point([(14-i, 14-i)], fill=(160, 25, 25))
    # Center pivot bolt
    d.ellipse([6, 6, 9, 9], fill=(130, 140, 155), outline=(60, 65, 75))
    # Hardened steel cutting blades
    d.polygon([(5, 5), (3, 1), (7, 4)], fill=(150, 160, 175))
    d.polygon([(10, 5), (12, 1), (8, 4)], fill=(120, 130, 145))
    d.line([(3, 1), (5, 5)], fill=(210, 220, 235))
    im.save(os.path.join(ITEMS_DIR, "wire_cutters.png"))

def make_wire_coil(name, wire_col, highlight_col, spool_steel=False):
    im = new_img()
    d = ImageDraw.Draw(im)
    flange_col = (70, 75, 85) if spool_steel else (105, 68, 38)
    flange_hi = (110, 120, 135) if spool_steel else (145, 95, 55)
    # Wooden/Steel spool flanges
    d.ellipse([1, 4, 6, 12], fill=flange_col, outline=flange_hi)
    d.ellipse([10, 4, 15, 12], fill=flange_col, outline=flange_hi)
    # Wound wire core
    d.rectangle([4, 5, 12, 11], fill=wire_col)
    for x in range(5, 12, 2):
        d.line([(x, 5), (x, 11)], fill=highlight_col)
    # Center spindle axle hole
    d.ellipse([2, 7, 4, 9], fill=(20, 20, 24))
    d.ellipse([11, 7, 13, 9], fill=(20, 20, 24))
    im.save(os.path.join(ITEMS_DIR, f"{name}.png"))

def make_material_item(name, color_palette):
    im = new_img()
    d = ImageDraw.Draw(im)
    if name == "treated_wood":
        d.rectangle([2, 4, 13, 11], fill=(55, 38, 25), outline=(35, 22, 14))
        d.line([(3, 6), (12, 6)], fill=(68, 48, 32))
        d.line([(3, 9), (12, 9)], fill=(44, 29, 19))
    elif "wire" in name or "strand" in name:
        base, hi = color_palette
        for i in range(12):
            d.point([(2+i, 13-i)], fill=base)
            d.point([(3+i, 13-i)], fill=hi)
            d.point([(1+i, 14-i)], fill=(base[0]-30, base[1]-30, base[2]-30))
    elif name == "ceramic_insulator":
        d.rectangle([5, 2, 10, 13], fill=(230, 235, 240))
        d.line([(4, 4), (11, 4)], fill=(255, 255, 255))
        d.line([(4, 7), (11, 7)], fill=(255, 255, 255))
        d.line([(4, 10), (11, 10)], fill=(255, 255, 255))
        d.line([(5, 5), (10, 5)], fill=(180, 185, 195))
        d.line([(5, 8), (10, 8)], fill=(180, 185, 195))
        d.line([(5, 11), (10, 11)], fill=(180, 185, 195))
    im.save(os.path.join(ITEMS_DIR, f"{name}.png"))

# Run all texture generators
make_treated_wood_pole()
make_steel_truss_pole()
make_pole_crossarm()
make_connector("lv_wire_connector", (200, 105, 45), (215, 205, 190), (255, 160, 85))
make_connector("mv_wire_connector", (225, 185, 50), (180, 120, 85), (255, 225, 110))
make_connector("hv_wire_connector", (90, 100, 115), (235, 240, 245), (150, 165, 185))
make_relay("lv_wire_relay", (215, 205, 190), (200, 105, 45))
make_relay("mv_wire_relay", (180, 120, 85), (225, 185, 50))
make_relay("hv_wire_relay", (235, 240, 245), (90, 100, 115))
make_transformer()
make_breaker_switch()
make_heavy_accumulator()
make_generator_casing()
make_alternator_coil()
make_turbine_rotor()
make_exhaust_stack()
make_power_plant_controller()

make_engineer_hammer()
make_voltmeter()
make_wire_cutters()
make_wire_coil("lv_wire_coil", (195, 100, 40), (245, 145, 75), spool_steel=False)
make_wire_coil("mv_wire_coil", (220, 175, 45), (255, 215, 95), spool_steel=False)
make_wire_coil("hv_wire_coil", (120, 130, 145), (185, 195, 210), spool_steel=True)
make_material_item("treated_wood", None)
make_material_item("copper_wire", ((200, 105, 45), (245, 145, 75)))
make_material_item("electrum_wire", ((225, 185, 50), (255, 225, 110)))
make_material_item("steel_strand", ((130, 140, 155), (185, 195, 210)))
make_material_item("ceramic_insulator", None)

print("All textures created!")

# --- BLOCK MODEL & BLOCKSTATE GENERATION ---

BLOCKS = [
    "treated_wood_pole", "steel_truss_pole", "pole_crossarm",
    "lv_wire_connector", "mv_wire_connector", "hv_wire_connector",
    "lv_wire_relay", "mv_wire_relay", "hv_wire_relay",
    "transformer", "breaker_switch", "heavy_accumulator",
    "power_plant_controller", "generator_casing", "alternator_coil",
    "turbine_rotor", "exhaust_stack"
]

ITEMS = [
    "engineer_hammer", "voltmeter", "wire_cutters",
    "lv_wire_coil", "mv_wire_coil", "hv_wire_coil",
    "treated_wood", "copper_wire", "electrum_wire", "steel_strand", "ceramic_insulator"
]

for b in BLOCKS:
    # 1. Block Model
    if b in ["treated_wood_pole", "steel_truss_pole", "generator_casing", "alternator_coil", "turbine_rotor"]:
        bm = {
            "parent": "minecraft:block/cube_all",
            "textures": {
                "all": f"powergrid:block/{b}"
            }
        }
    elif b == "pole_crossarm":
        bm = {
            "parent": "minecraft:block/cube_all",
            "textures": {
                "all": "powergrid:block/pole_crossarm"
            }
        }
    elif "wire_connector" in b or "wire_relay" in b:
        bm = {
            "parent": "minecraft:block/cube_all",
            "textures": {
                "all": f"powergrid:block/{b}"
            }
        }
    elif b == "transformer":
        bm = {
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
        }
    elif b == "heavy_accumulator":
        bm = {
            "parent": "minecraft:block/cube_column",
            "textures": {
                "end": "powergrid:block/heavy_accumulator_top",
                "side": "powergrid:block/heavy_accumulator_side"
            }
        }
    elif b == "exhaust_stack":
        bm = {
            "parent": "minecraft:block/cube_column",
            "textures": {
                "end": "powergrid:block/exhaust_stack_top",
                "side": "powergrid:block/exhaust_stack_side"
            }
        }
    elif b == "power_plant_controller":
        bm = {
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
        }
    elif b == "breaker_switch":
        bm = {
            "parent": "minecraft:block/cube_all",
            "textures": {
                "all": "powergrid:block/breaker_switch"
            }
        }
    write_json(os.path.join(MODELS_BLOCK, f"{b}.json"), bm)

    # 2. Blockstate
    if b in ["treated_wood_pole", "steel_truss_pole", "generator_casing", "alternator_coil", "turbine_rotor", "heavy_accumulator", "exhaust_stack"]:
        bs = {
            "variants": {
                "": {"model": f"powergrid:block/{b}"}
            }
        }
    elif b in ["pole_crossarm", "transformer"]:
        bs = {
            "variants": {
                "facing=north": {"model": f"powergrid:block/{b}", "y": 0},
                "facing=east":  {"model": f"powergrid:block/{b}", "y": 90},
                "facing=south": {"model": f"powergrid:block/{b}", "y": 180},
                "facing=west":  {"model": f"powergrid:block/{b}", "y": 270}
            }
        }
    elif "wire_connector" in b or "wire_relay" in b:
        bs = {
            "variants": {
                "facing=down":  {"model": f"powergrid:block/{b}", "x": 180},
                "facing=up":    {"model": f"powergrid:block/{b}"},
                "facing=north": {"model": f"powergrid:block/{b}", "x": 90},
                "facing=south": {"model": f"powergrid:block/{b}", "x": 90, "y": 180},
                "facing=west":  {"model": f"powergrid:block/{b}", "x": 90, "y": 270},
                "facing=east":  {"model": f"powergrid:block/{b}", "x": 90, "y": 90}
            }
        }
    elif b == "breaker_switch":
        bs = {
            "variants": {
                "facing=north,open=false,powered=false": {"model": "powergrid:block/breaker_switch"},
                "facing=north,open=false,powered=true":  {"model": "powergrid:block/breaker_switch"},
                "facing=north,open=true,powered=false":  {"model": "powergrid:block/breaker_switch"},
                "facing=north,open=true,powered=true":   {"model": "powergrid:block/breaker_switch"},
                "facing=south,open=false,powered=false": {"model": "powergrid:block/breaker_switch", "y": 180},
                "facing=south,open=false,powered=true":  {"model": "powergrid:block/breaker_switch", "y": 180},
                "facing=south,open=true,powered=false":  {"model": "powergrid:block/breaker_switch", "y": 180},
                "facing=south,open=true,powered=true":   {"model": "powergrid:block/breaker_switch", "y": 180},
                "facing=west,open=false,powered=false":  {"model": "powergrid:block/breaker_switch", "y": 270},
                "facing=west,open=false,powered=true":   {"model": "powergrid:block/breaker_switch", "y": 270},
                "facing=west,open=true,powered=false":   {"model": "powergrid:block/breaker_switch", "y": 270},
                "facing=west,open=true,powered=true":    {"model": "powergrid:block/breaker_switch", "y": 270},
                "facing=east,open=false,powered=false":  {"model": "powergrid:block/breaker_switch", "y": 90},
                "facing=east,open=false,powered=true":   {"model": "powergrid:block/breaker_switch", "y": 90},
                "facing=east,open=true,powered=false":   {"model": "powergrid:block/breaker_switch", "y": 90},
                "facing=east,open=true,powered=true":    {"model": "powergrid:block/breaker_switch", "y": 90}
            }
        }
    elif b == "power_plant_controller":
        bs = {
            "variants": {
                "active=false,assembled=false,facing=north": {"model": "powergrid:block/power_plant_controller"},
                "active=false,assembled=false,facing=south": {"model": "powergrid:block/power_plant_controller", "y": 180},
                "active=false,assembled=false,facing=west":  {"model": "powergrid:block/power_plant_controller", "y": 270},
                "active=false,assembled=false,facing=east":  {"model": "powergrid:block/power_plant_controller", "y": 90},
                "active=false,assembled=true,facing=north":  {"model": "powergrid:block/power_plant_controller"},
                "active=false,assembled=true,facing=south":  {"model": "powergrid:block/power_plant_controller", "y": 180},
                "active=false,assembled=true,facing=west":   {"model": "powergrid:block/power_plant_controller", "y": 270},
                "active=false,assembled=true,facing=east":   {"model": "powergrid:block/power_plant_controller", "y": 90},
                "active=true,assembled=false,facing=north":  {"model": "powergrid:block/power_plant_controller"},
                "active=true,assembled=false,facing=south":  {"model": "powergrid:block/power_plant_controller", "y": 180},
                "active=true,assembled=false,facing=west":   {"model": "powergrid:block/power_plant_controller", "y": 270},
                "active=true,assembled=false,facing=east":   {"model": "powergrid:block/power_plant_controller", "y": 90},
                "active=true,assembled=true,facing=north":   {"model": "powergrid:block/power_plant_controller"},
                "active=true,assembled=true,facing=south":   {"model": "powergrid:block/power_plant_controller", "y": 180},
                "active=true,assembled=true,facing=west":    {"model": "powergrid:block/power_plant_controller", "y": 270},
                "active=true,assembled=true,facing=east":    {"model": "powergrid:block/power_plant_controller", "y": 90}
            }
        }
    write_json(os.path.join(BLOCKSTATES, f"{b}.json"), bs)

    # 3. Item Model (points to block model)
    write_json(os.path.join(MODELS_ITEM, f"{b}.json"), {
        "parent": f"powergrid:block/{b}"
    })

    # 4. Item Definition in items/<name>.json (Modern 26.3 specification!)
    write_json(os.path.join(ITEM_DEFS, f"{b}.json"), {
        "model": {
            "type": "minecraft:model",
            "model": f"powergrid:item/{b}"
        }
    })

# --- REGULAR ITEMS GENERATION ---

for it in ITEMS:
    # 1. Item Model
    write_json(os.path.join(MODELS_ITEM, f"{it}.json"), {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": f"powergrid:item/{it}"
        }
    })

    # 2. Item Definition in items/<name>.json
    write_json(os.path.join(ITEM_DEFS, f"{it}.json"), {
        "model": {
            "type": "minecraft:model",
            "model": f"powergrid:item/{it}"
        }
    })

print("All blockstates, models, and item definitions successfully generated!")
