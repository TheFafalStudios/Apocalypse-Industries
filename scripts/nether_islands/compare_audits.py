"""Compare native terrain audit maps and export a diagnostic image and summary."""
from pathlib import Path
import argparse, json
from collections import Counter
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]

def main():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('before', type=Path)
    p.add_argument('after', type=Path)
    a = p.parse_args()
    old, new = [json.loads(path.read_text()) for path in (a.before, a.after)]
    assert old['mapCenter'] == new['mapCenter']
    assert old['pass'] and new['pass']
    summary = {}
    for key, data in [('before', old), ('after', new)]:
        values = data.get('actualHeights', data['baseHeights'])
        shift_counts = Counter(height-data['baseHeights'][z][x]
            for z,row in enumerate(values) for x,height in enumerate(row)
            if height >= 0 and data['baseHeights'][z][x] >= 0)
        delta = shift_counts.most_common(1)[0][0]
        sea = 32 + delta
        changes = [0] * 4
        shore_steps = [0] * 4
        center_x, center_z = data['mapCenter']
        for z, row in enumerate(values):
            for x, height in enumerate(row):
                for previous, coord in ([(row[x-1], center_x+x-64)] if x else []) + (
                    [(values[z-1][x], center_z+z-64)] if z else []):
                    if height != previous:
                        changes[coord % 4] += 1
                    if (height >= sea) != (previous >= sea):
                        shore_steps[coord % 4] += 1
        summary[key] = {k:v for k,v in data.items() if k not in ['baseHeights', 'actualHeights', 'structures']}
        shifts = {}
        for z,row in enumerate(values):
            for x,height in enumerate(row):
                expected = data['baseHeights'][z][x]
                if height >= 0 and expected >= 0:
                    delta = str(height-expected)
                    shifts[delta] = shifts.get(delta,0)+1
        summary[key]['actualMinusExpectedHeightHistogram'] = shifts
        summary[key]['actualLavaThresholdUsedForOutline'] = sea
        summary[key]['heightChangesByMod4'] = changes
        summary[key]['shorelineTransitionsByMod4'] = shore_steps
        summary[key]['structures'] = {
            k:{name:value for name,value in v.items() if name != 'samples'}
            for k,v in data.get('structures', {}).items()}
    image = Image.new('RGB', (1120, 690), '#13171e')
    draw = ImageDraw.Draw(image)
    font_path = Path('C:/Windows/Fonts/segoeui.ttf')
    title = ImageFont.truetype(str(font_path), 25)
    body = ImageFont.truetype(str(font_path), 17)
    draw.text((24, 15), 'Nether island outlines — same seed, heights aligned', fill='white', font=title)
    for index, (label, data) in enumerate([('Previous preset', old), ('Smoother density fields', new)]):
        tile = Image.new('RGB', (128,128))
        for z, row in enumerate(data.get('actualHeights', data['baseHeights'])):
            for x, height in enumerate(row):
                sea = summary['before' if index==0 else 'after']['actualLavaThresholdUsedForOutline']
                aligned = height + 32 - sea
                color = (192,63,18) if height < sea else (
                    min(255,95+aligned), min(255,69+aligned), min(255,50+aligned))
                tile.putpixel((x,z), color)
        x0=24+index*552
        image.paste(tile.resize((512,512),Image.Resampling.NEAREST),(x0,99))
        draw.text((x0,65),label,fill='white',font=title)
    draw.text((24,625),'Orange = lava / no land above lava. Light = higher terrain. 128 × 128 blocks per panel.',
              fill='#c3c9d2',font=body)
    draw.text((24,653),'Baseline is raised 16 blocks here to compare shapes independently of the corrected height bug.',
              fill='#c3c9d2',font=body)
    assets = ROOT / 'docs/assets'
    assets.mkdir(exist_ok=True)
    image.save(assets / 'nether-islands-density-comparison.png')
    (ROOT / 'docs/NETHER_ISLANDS_VALIDATION.json').write_text(json.dumps(summary,indent=2)+'\n')
    print(json.dumps(summary,indent=2))

if __name__ == '__main__':
    main()
