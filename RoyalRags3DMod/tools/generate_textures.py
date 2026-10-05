from pathlib import Path
import struct, zlib, random

W = H = 128
OUT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/royalrags/textures/armor"
OUT.mkdir(parents=True, exist_ok=True)

PAL = {
    "leather": ((82,54,38,255),(46,31,25,255),(128,90,58,255),(155,126,83,255),(91,74,52,255)),
    "chainmail": ((28,29,34,255),(14,15,19,255),(74,74,82,255),(145,24,29,255),(210,164,48,255)),
    "iron": ((29,31,35,255),(11,12,14,255),(220,222,217,255),(168,24,28,255),(87,93,100,255)),
    "gold": ((235,228,201,255),(86,58,10,255),(255,249,230,255),(226,166,22,255),(82,62,27,255)),
    "diamond": ((29,83,163,255),(14,37,91,255),(84,182,235,255),(233,186,48,255),(226,229,231,255)),
    "netherite": ((31,26,31,255),(11,9,12,255),(74,63,69,255),(159,17,27,255),(228,172,45,255)),
}

def png(path, pixels):
    row_bytes = W * 4
    raw = b"".join(
        b"\x00" + bytes(pixels[y*row_bytes:(y+1)*row_bytes])
        for y in range(H)
    )
    def chunk(t, d):
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t+d) & 0xffffffff)
    data = b"\x89PNG\r\n\x1a\n"
    data += chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0))
    data += chunk(b"IDAT", zlib.compress(raw, 9))
    data += chunk(b"IEND", b"")
    path.write_bytes(data)

def rect(px, x0, y0, x1, y1, c):
    x0=max(0,x0); y0=max(0,y0); x1=min(W-1,x1); y1=min(H-1,y1)
    for y in range(y0, y1+1):
        for x in range(x0, x1+1):
            i=(y*W+x)*4
            px[i:i+4]=bytes(c)

def point(px, x, y, c):
    if 0 <= x < W and 0 <= y < H:
        i=(y*W+x)*4
        px[i:i+4]=bytes(c)

def make(name, p, seed):
    base,dark,light,accent,accent2=p
    rng=random.Random(seed)
    px=bytearray(W*H*4)

    for y in range(H):
        for x in range(W):
            c=base
            r=rng.random()
            if r < .05:
                c=tuple(int(base[i]*.84+dark[i]*.16) for i in range(3))+(255,)
            elif r > .95:
                c=tuple(int(base[i]*.84+light[i]*.16) for i in range(3))+(255,)
            i=(y*W+x)*4
            px[i:i+4]=bytes(c)

    rect(px,0,0,31,1,dark); rect(px,0,14,31,15,dark)
    rect(px,16,16,39,17,accent); rect(px,16,30,39,31,dark)
    rect(px,16,16,17,31,dark); rect(px,38,16,39,31,dark)
    rect(px,0,16,15,17,dark); rect(px,40,16,55,17,dark)

    rect(px,64,0,127,127,base)
    for y in range(0,128,16):
        rect(px,64,y,127,min(127,y+1),dark)
    for x in range(64,128,16):
        rect(px,x,0,min(127,x+1),127,light)

    if name == "leather":
        rect(px,64,24,95,28,accent); rect(px,80,24,81,63,dark)
        for x,y in [(6,20),(20,23),(29,11),(47,26),(72,40),(95,55),(105,22)]:
            rect(px,x,y,x+2,y+1,accent2)
        rect(px,64,49,96,51,dark)
    elif name == "chainmail":
        for y in range(0,128,4):
            for x in range(0,128,4):
                if (x//4+y//4)%2 == 0:
                    point(px,x,y,light)
        rect(px,64,20,96,23,accent); rect(px,86,20,104,23,accent2)
    elif name == "iron":
        rect(px,18,16,37,31,(238,238,232,255)); rect(px,26,17,29,29,accent)
        rect(px,64,0,100,7,dark); rect(px,64,36,100,41,dark); rect(px,64,52,100,63,(18,18,20,255))
    elif name == "gold":
        rect(px,0,0,63,31,light); rect(px,16,16,39,31,light)
        rect(px,16,16,17,31,accent); rect(px,38,16,39,31,accent)
        rect(px,64,0,127,127,light)
        for y in range(0,128,14):
            rect(px,64,y,127,min(127,y+2),accent)
        rect(px,88,24,110,27,accent2)
    elif name == "diamond":
        rect(px,16,16,39,31,base); rect(px,26,16,29,31,accent2); rect(px,16,16,39,17,accent)
        rect(px,64,0,127,127,base)
        for y in (0,14,28,48,66):
            rect(px,64,y,127,min(127,y+2),accent)
        rect(px,88,28,127,44,(35,65,135,255)); rect(px,110,28,124,34,accent2)
    elif name == "netherite":
        rect(px,16,16,39,31,base); rect(px,26,16,29,31,accent); rect(px,16,16,39,17,accent2)
        rect(px,64,0,127,127,base); rect(px,64,32,127,36,(224,218,204,255))
        for x in range(66,126,8):
            rect(px,x,33,x+2,34,(90,80,75,255))
        rect(px,64,76,127,93,(89,12,22,255)); rect(px,64,76,127,78,accent2)
        rect(px,64,96,127,100,accent2)
        rect(px,94,76,100,84,(193,16,32,255)); rect(px,96,78,98,82,(255,95,95,255))
        rect(px,64,114,127,127,dark); rect(px,64,114,127,116,accent2)

    return px

for i,(name,p) in enumerate(PAL.items()):
    png(OUT/f"{name}.png", make(name,p,42+i))
    print("generated", name)
