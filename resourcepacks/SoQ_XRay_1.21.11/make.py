#!/usr/bin/env python3
"""SoQ X-Ray + glowing ore edges for Minecraft Java 1.21.11."""
import io, json, pathlib, zipfile
from PIL import Image, ImageDraw

HERE=pathlib.Path(__file__).parent
COLORS={
'coal_ore':'#E5EDFF','iron_ore':'#FFE0A8','copper_ore':'#FF9258',
'gold_ore':'#FFE04A','redstone_ore':'#FF3D55','emerald_ore':'#43FF89',
'lapis_ore':'#528BFF','diamond_ore':'#56F8FF',
'nether_gold_ore':'#FFE04A','nether_quartz_ore':'#F4F5FF',
'ancient_debris':'#DCA2FF','amethyst_block':'#E2ADFF',
'budding_amethyst':'#E2ADFF'
}
for ore in ('coal','iron','copper','gold','redstone','emerald','lapis','diamond'):
    COLORS['deepslate_'+ore+'_ore']=COLORS[ore+'_ore']
HIDDEN='''stone granite andesite diorite deepslate tuff calcite dripstone_block
dirt coarse_dirt rooted_dirt grass_block gravel sand red_sand clay mud mud_bricks
podzol mycelium netherrack soul_sand soul_soil blackstone basalt smooth_basalt
end_stone terracotta white_terracotta orange_terracotta magenta_terracotta
light_blue_terracotta yellow_terracotta lime_terracotta pink_terracotta
gray_terracotta light_gray_terracotta cyan_terracotta purple_terracotta
blue_terracotta brown_terracotta green_terracotta red_terracotta
black_terracotta sandstone red_sandstone crimson_nylium warped_nylium'''.split()
DIRS=('north','south','east','west','up','down')
def jb(x): return json.dumps(x,ensure_ascii=False,separators=(',',':')).encode()
def png(im):
    b=io.BytesIO();im.save(b,'PNG');return b.getvalue()
def img():
    im=Image.new('RGBA',(128,128),'#141B26')
    d=ImageDraw.Draw(im)
    d.polygon([(64,18),(110,43),(64,68),(18,43)],fill='#314355')
    d.polygon([(18,43),(64,68),(64,117),(18,91)],fill='#20394A')
    d.polygon([(64,68),(110,43),(110,91),(64,117)],fill='#163948')
    for a,b in [((64,18),(110,43)),((110,43),(110,91)),((110,91),(64,117)),((64,117),(18,91)),((18,91),(18,43)),((18,43),(64,18)),((18,43),(64,68)),((110,43),(64,68)),((64,68),(64,117))]:
        d.line([a,b],fill='#52F3FF',width=3)
    for rect,color in [((44,49,53,59),'#56F8FF'),((70,51,83,65),'#FFDC52'),((74,75,83,85),'#46FF83'),((42,75,50,83),'#FE4A5F')]: d.rectangle(rect,fill=color)
    return png(im)
def lines():
    out=[];p=.62;offset=.05
    for face in DIRS:
        if face in ('north','south'):
            z=-offset if face=='north' else 16+offset
            segments=[([0,0,z],[16,p,z]),([0,16-p,z],[16,16,z]),
                      ([0,0,z],[p,16,z]),([16-p,0,z],[16,16,z])]
        elif face in ('east','west'):
            x=-offset if face=='west' else 16+offset
            segments=[([x,0,0],[x,p,16]),([x,16-p,0],[x,16,16]),
                      ([x,0,0],[x,16,p]),([x,0,16-p],[x,16,16])]
        else:
            y=-offset if face=='down' else 16+offset
            segments=[([0,y,0],[p,y,16]),([16-p,y,0],[16,y,16]),
                      ([0,y,0],[16,y,p]),([0,y,16-p],[16,y,16])]
        for fr,to in segments:
            out.append({'from':fr,'to':to,'shade':False,'light_emission':15,
                        'faces':{face:{'texture':'#outline','uv':[0,0,16,16]}}})
    return out
BANDS=lines()
def model(name):
    faces={d:{'texture':'#base'} for d in DIRS}
    tex={'base':'minecraft:block/'+('ancient_debris_side' if name=='ancient_debris' else name),
         'outline':'soq:ore_outline/'+name,'particle':'#base'}
    if name=='ancient_debris':
        tex['top']='minecraft:block/ancient_debris_top'
        for d in ('up','down'):faces[d]['texture']='#top'
    return {'parent':'minecraft:block/block','ambientocclusion':False,'textures':tex,
            'elements':[{'from':[0,0,0],'to':[16,16,16],'faces':faces}]+BANDS}
def build(xray):
    filename='SoQ_XRay_GlowOres_1.21.11.zip' if xray else 'SoQ_GlowOres_Only_1.21.11.zip'
    files={'pack.mcmeta':jb({'pack':{'pack_format':75,'min_format':[75,0],'max_format':[75,0],
        'description':'SoQ | '+('X-RAY + ' if xray else '')+'GLOW ORE OUTLINES | Java 1.21.11'}}),
        'pack.png':img()}
    for name,color in COLORS.items():
        files['assets/minecraft/models/block/'+name+'.json']=jb(model(name))
        files['assets/soq/textures/ore_outline/'+name+'.png']=png(Image.new('RGBA',(16,16),color))
    if xray:
        for name in HIDDEN:
            files['assets/minecraft/models/block/'+name+'.json']=jb({
                'parent':'minecraft:block/block','textures':{'particle':'minecraft:block/stone'},
                'elements':[]})
    files['README_RU.txt']=(
        'SoQ Minecraft Java 1.21.11\\n'
        'Поместите ZIP без распаковки в папку .minecraft/resourcepacks.\\n'
        'В игре: Настройки -> Наборы ресурсов -> включить.\\n'
        'Светящиеся контуры руд используют vanilla light_emission=15.\\n'
        'Это яркие модели, а не источники освещения соседних блоков.\\n'
        + ('X-Ray скрывает камень, землю и другие блоки.\\n' if xray else
           'Этот вариант не скрывает обычные блоки.\\n')).encode()
    path=HERE/filename
    with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
        for k,v in files.items():z.writestr(k,v)
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None
    print(path)
if __name__=='__main__':
    build(True);build(False)
