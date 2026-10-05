from pathlib import Path
import random
import struct
import zlib

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources"
ARMOR_OUT = ROOT / "assets/royalrags/textures/armor"
ITEM_OUT = ROOT / "assets/minecraft/textures/item"
ARMOR_OUT.mkdir(parents=True, exist_ok=True)
ITEM_OUT.mkdir(parents=True, exist_ok=True)

ATLAS_W = ATLAS_H = 256
CELL = 32
CELL_Y = 64

SEMANTICS = [
    "base", "dark", "light", "accent", "metal", "gold", "white", "fur",
    "gem", "secondary", "dirty", "chain", "red", "blue", "shoe", "cape"
]

PALETTES = {
    "leather": {
        "base": (87, 57, 38, 255), "dark": (45, 31, 24, 255), "light": (161, 125, 79, 255),
        "accent": (117, 92, 58, 255), "metal": (92, 92, 86, 255), "gold": (164, 119, 45, 255),
        "white": (178, 158, 122, 255), "fur": (165, 143, 108, 255), "gem": (78, 103, 72, 255),
        "secondary": (70, 55, 42, 255), "dirty": (113, 93, 63, 255), "chain": (102, 101, 94, 255),
        "red": (121, 50, 39, 255), "blue": (58, 79, 84, 255), "shoe": (55, 41, 31, 255),
        "cape": (82, 57, 39, 255)
    },
    "chainmail": {
        "base": (24, 25, 31, 255), "dark": (12, 13, 17, 255), "light": (116, 119, 126, 255),
        "accent": (150, 27, 32, 255), "metal": (132, 138, 145, 255), "gold": (214, 164, 43, 255),
        "white": (192, 194, 197, 255), "fur": (118, 118, 121, 255), "gem": (178, 36, 42, 255),
        "secondary": (50, 52, 60, 255), "dirty": (77, 70, 65, 255), "chain": (94, 99, 108, 255),
        "red": (168, 25, 31, 255), "blue": (41, 61, 79, 255), "shoe": (18, 19, 23, 255),
        "cape": (54, 14, 18, 255)
    },
    "iron": {
        "base": (31, 32, 37, 255), "dark": (11, 12, 15, 255), "light": (218, 220, 216, 255),
        "accent": (166, 28, 32, 255), "metal": (159, 165, 170, 255), "gold": (186, 145, 54, 255),
        "white": (238, 238, 233, 255), "fur": (210, 210, 205, 255), "gem": (139, 26, 31, 255),
        "secondary": (82, 86, 91, 255), "dirty": (98, 91, 82, 255), "chain": (118, 123, 128, 255),
        "red": (176, 28, 34, 255), "blue": (50, 75, 95, 255), "shoe": (16, 17, 20, 255),
        "cape": (27, 28, 32, 255)
    },
    "gold": {
        "base": (236, 230, 209, 255), "dark": (30, 28, 25, 255), "light": (255, 251, 238, 255),
        "accent": (225, 170, 30, 255), "metal": (226, 175, 34, 255), "gold": (240, 187, 35, 255),
        "white": (247, 243, 228, 255), "fur": (238, 234, 222, 255), "gem": (74, 57, 31, 255),
        "secondary": (100, 81, 48, 255), "dirty": (171, 146, 101, 255), "chain": (222, 176, 39, 255),
        "red": (135, 37, 34, 255), "blue": (57, 83, 108, 255), "shoe": (243, 239, 221, 255),
        "cape": (243, 238, 218, 255)
    },
    "diamond": {
        "base": (27, 79, 158, 255), "dark": (15, 36, 83, 255), "light": (104, 187, 231, 255),
        "accent": (231, 184, 48, 255), "metal": (212, 170, 46, 255), "gold": (236, 188, 46, 255),
        "white": (235, 238, 242, 255), "fur": (230, 234, 239, 255), "gem": (110, 221, 246, 255),
        "secondary": (53, 112, 184, 255), "dirty": (89, 105, 117, 255), "chain": (204, 166, 52, 255),
        "red": (156, 43, 50, 255), "blue": (33, 102, 197, 255), "shoe": (29, 73, 143, 255),
        "cape": (30, 88, 172, 255)
    },
    "netherite": {
        "base": (32, 28, 33, 255), "dark": (11, 10, 13, 255), "light": (77, 68, 75, 255),
        "accent": (127, 19, 29, 255), "metal": (99, 82, 67, 255), "gold": (222, 166, 42, 255),
        "white": (229, 224, 215, 255), "fur": (224, 220, 211, 255), "gem": (203, 29, 42, 255),
        "secondary": (73, 54, 58, 255), "dirty": (82, 71, 66, 255), "chain": (164, 126, 42, 255),
        "red": (137, 15, 29, 255), "blue": (48, 66, 91, 255), "shoe": (22, 19, 23, 255),
        "cape": (109, 12, 24, 255)
    },
}

ITEM_PREFIX = {
    "leather": "leather",
    "chainmail": "chainmail",
    "iron": "iron",
    "gold": "golden",
    "diamond": "diamond",
    "netherite": "netherite",
}


def png_bytes(width, height, px):
    row_bytes = width * 4
    raw = b"".join(
        b"\x00" + bytes(px[y * row_bytes:(y + 1) * row_bytes])
        for y in range(height)
    )

    def chunk(tag, data):
        return (
            struct.pack(">I", len(data))
            + tag + data
            + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
        )

    return (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )


def set_px(px, width, height, x, y, c):
    if 0 <= x < width and 0 <= y < height:
        i = (y * width + x) * 4
        px[i:i + 4] = bytes(c)


def rect(px, width, height, x0, y0, x1, y1, c):
    x0 = max(0, int(x0))
    y0 = max(0, int(y0))
    x1 = min(width - 1, int(x1))
    y1 = min(height - 1, int(y1))
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            set_px(px, width, height, x, y, c)


def mix(a, b, t):
    return tuple(int(a[i] * (1 - t) + b[i] * t) for i in range(3)) + (255,)


def shade(c, amount):
    if amount >= 0:
        return mix(c, (255, 255, 255, 255), amount)
    return mix(c, (0, 0, 0, 255), -amount)


def paint_cell(px, style, semantic, index, rng):
    pal = PALETTES[style]
    base = pal[semantic]
    x0 = (index % 8) * CELL
    y0 = CELL_Y + (index // 8) * CELL

    # Multi-tone textile/metal base.
    for y in range(y0, y0 + CELL):
        for x in range(x0, x0 + CELL):
            local = (x - x0) / (CELL - 1)
            c = shade(base, 0.08 * (0.5 - local))
            r = rng.random()
            if r < 0.055:
                c = shade(c, -0.16)
            elif r > 0.955:
                c = shade(c, 0.16)
            set_px(px, ATLAS_W, ATLAS_H, x, y, c)

    # Dark edge gives every cuboid a pixel-art beveled look.
    edge = shade(base, -0.24)
    rect(px, ATLAS_W, ATLAS_H, x0, y0, x0 + CELL - 1, y0 + 1, edge)
    rect(px, ATLAS_W, ATLAS_H, x0, y0, x0 + 1, y0 + CELL - 1, edge)
    hi = shade(base, 0.20)
    rect(px, ATLAS_W, ATLAS_H, x0 + 2, y0 + 2, x0 + CELL - 3, y0 + 2, hi)

    # Semantic-specific micro detail.
    if semantic == "gold":
        for y in range(y0 + 5, y0 + CELL - 3, 7):
            rect(px, ATLAS_W, ATLAS_H, x0 + 3, y, x0 + CELL - 4, y + 1, shade(base, 0.22))
        for x in range(x0 + 6, x0 + CELL - 4, 8):
            set_px(px, ATLAS_W, ATLAS_H, x, y0 + 5, shade(base, 0.45))
    elif semantic == "chain":
        for y in range(y0 + 3, y0 + CELL - 2, 4):
            for x in range(x0 + 3, x0 + CELL - 2, 4):
                if ((x + y) // 4) % 2 == 0:
                    set_px(px, ATLAS_W, ATLAS_H, x, y, shade(base, 0.35))
                    set_px(px, ATLAS_W, ATLAS_H, x + 1, y + 1, shade(base, -0.25))
    elif semantic == "fur":
        for _ in range(130):
            x = rng.randrange(x0 + 2, x0 + CELL - 2)
            y = rng.randrange(y0 + 2, y0 + CELL - 2)
            set_px(px, ATLAS_W, ATLAS_H, x, y, shade(base, rng.choice([-0.25, -0.12, 0.15, 0.25])))
    elif semantic == "gem":
        inner = shade(base, 0.28)
        rect(px, ATLAS_W, ATLAS_H, x0 + 7, y0 + 6, x0 + CELL - 8, y0 + CELL - 7, inner)
        rect(px, ATLAS_W, ATLAS_H, x0 + 9, y0 + 8, x0 + 12, y0 + 11, shade(base, 0.55))
        rect(px, ATLAS_W, ATLAS_H, x0 + CELL - 12, y0 + CELL - 11, x0 + CELL - 9, y0 + CELL - 8, shade(base, -0.35))
    elif semantic in ("red", "blue", "accent", "cape"):
        for y in range(y0 + 6, y0 + CELL - 2, 8):
            rect(px, ATLAS_W, ATLAS_H, x0 + 4, y, x0 + CELL - 5, y, shade(base, -0.12))
    elif semantic in ("white", "light"):
        for y in range(y0 + 5, y0 + CELL - 3, 6):
            rect(px, ATLAS_W, ATLAS_H, x0 + 5, y, x0 + CELL - 6, y, shade(base, -0.08))
    elif semantic in ("dirty", "base", "secondary"):
        for _ in range(30):
            x = rng.randrange(x0 + 3, x0 + CELL - 3)
            y = rng.randrange(y0 + 3, y0 + CELL - 3)
            set_px(px, ATLAS_W, ATLAS_H, x, y, shade(base, -0.20))


def build_atlas(style):
    # Transparent top 64px is deliberate: vanilla parent armor cubes sample there,
    # so only our custom garment cuboids are visible.
    px = bytearray(ATLAS_W * ATLAS_H * 4)
    rng = random.Random(9000 + sum(ord(c) for c in style))

    for i, semantic in enumerate(SEMANTICS):
        paint_cell(px, style, semantic, i, rng)

    # Tiny fabric stitches in unused lower region so no accidental solid fallback.
    # Keep it transparent elsewhere.
    (ARMOR_OUT / f"{style}.png").write_bytes(png_bytes(ATLAS_W, ATLAS_H, px))


def icon_pixel_art(style, slot):
    pal = PALETTES[style]
    w = h = 16
    px = bytearray(w * h * 4)

    main = pal["base"]
    dark = pal["dark"]
    light = pal["white"] if style in ("gold", "iron") else pal["light"]
    trim = pal["gold"] if style in ("gold", "diamond", "netherite") else pal["accent"]
    red = pal["red"]
    gem = pal["gem"]

    def R(x0, y0, x1, y1, c):
        rect(px, w, h, x0, y0, x1, y1, c)

    if slot == "helmet":
        R(4, 2, 11, 3, trim)
        R(3, 4, 12, 10, main)
        R(4, 5, 11, 9, dark)
        R(5, 6, 10, 10, (0, 0, 0, 0))
        if style in ("diamond", "netherite"):
            R(3, 1, 4, 4, trim); R(7, 0, 8, 4, trim); R(11, 1, 12, 4, trim)
            R(7, 2, 8, 3, gem)
        elif style == "chainmail":
            R(3, 3, 12, 4, red)
        elif style == "iron":
            R(4, 6, 6, 7, dark); R(9, 6, 11, 7, dark)
        elif style == "gold":
            R(4, 5, 6, 6, dark); R(9, 5, 11, 6, dark)
    elif slot == "chestplate":
        R(4, 2, 11, 12, main)
        R(2, 3, 4, 8, main); R(11, 3, 13, 8, main)
        R(5, 2, 10, 4, light)
        R(7, 4, 8, 10, trim)
        if style == "netherite":
            R(3, 2, 12, 3, pal["fur"])
            R(6, 5, 9, 7, gem)
            R(4, 4, 5, 11, red); R(10, 4, 11, 11, red)
        elif style == "diamond":
            R(3, 3, 4, 8, trim); R(11, 3, 12, 8, trim)
        elif style == "gold":
            R(5, 5, 10, 5, trim); R(6, 7, 9, 7, trim)
        elif style == "iron":
            R(7, 3, 8, 8, red)
        elif style == "chainmail":
            R(3, 4, 4, 10, red); R(11, 4, 12, 10, red)
        else:
            R(4, 6, 6, 9, pal["accent"])
    elif slot == "leggings":
        R(4, 2, 11, 5, main)
        R(4, 5, 7, 13, main); R(8, 5, 11, 13, main)
        R(4, 2, 11, 3, trim)
        if style in ("diamond", "gold"):
            R(4, 5, 4, 12, trim); R(11, 5, 11, 12, trim)
        elif style == "netherite":
            R(5, 7, 6, 8, trim); R(9, 7, 10, 8, trim)
        elif style == "chainmail":
            R(4, 5, 4, 12, red); R(11, 5, 11, 12, red)
    elif slot == "boots":
        R(3, 5, 6, 12, main); R(9, 5, 12, 12, main)
        R(2, 10, 6, 13, dark); R(9, 10, 13, 13, dark)
        R(2, 9, 6, 9, trim); R(9, 9, 13, 9, trim)
        if style in ("diamond", "netherite"):
            R(4, 6, 5, 9, light); R(10, 6, 11, 9, light)

    # one-pixel outlines / highlights
    for x in range(w):
        if any(px[(y * w + x) * 4 + 3] for y in range(h)):
            break

    return png_bytes(w, h, px)


def build_icons(style):
    prefix = ITEM_PREFIX[style]
    for slot in ("helmet", "chestplate", "leggings", "boots"):
        filename = f"{prefix}_{slot}.png"
        (ITEM_OUT / filename).write_bytes(icon_pixel_art(style, slot))


for style in PALETTES:
    build_atlas(style)
    build_icons(style)
    print("generated detailed atlas + icons:", style)
