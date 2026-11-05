"""Render current PlantUML diagrams locally, preserving relative shared includes.

Usage: python3 diagrams/render_plantuml.py --jar /path/to/plantuml.jar
Optional: --source-dir diagrams/classes [--output-dir /custom/export/folder]
"""
from pathlib import Path
import argparse
import html
import re
import shutil
import subprocess
import tempfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', required=True, type=Path)
    parser.add_argument('--source-dir', type=Path, help='Defaults to classes/ and sequence/')
    parser.add_argument('--output-dir', type=Path, help='Defaults to beside each source')
    args = parser.parse_args()
    jar = args.jar.expanduser().resolve()
    if not jar.is_file():
        parser.error(f'PlantUML JAR not found: {jar}')
    diagrams = Path(__file__).resolve().parent
    groups = [args.source_dir.resolve()] if args.source_dir else [diagrams / 'classes', diagrams / 'sequence']
    sources = []
    for group in groups:
        if group not in [diagrams / 'classes', diagrams / 'sequence']:
            parser.error('--source-dir must select diagrams/classes or diagrams/sequence')
        sources.extend(sorted(group.glob('*.puml')))
    if not sources:
        parser.error('No diagram sources found')
    with tempfile.TemporaryDirectory(prefix='restaurant-plantuml-') as temporary:
        build = Path(temporary) / 'diagrams'
        # Preserve the directory structure used by ../shared/ includes.
        shutil.copytree(diagrams, build, ignore=shutil.ignore_patterns('*.svg', '__pycache__'))
        rendered_sources = [build / source.relative_to(diagrams) for source in sources]
        subprocess.run([
            'java', '-Djava.awt.headless=true', '-jar', str(jar),
            '-charset', 'UTF-8', '-failfast2', '-tsvg',
            *map(str, rendered_sources),
        ], check=True)
        outputs = []
        for source, rendered in zip(sources, rendered_sources):
            svg = rendered.with_suffix('.svg').read_text(encoding='utf-8')
            if '<svg ' not in svg or 'data-diagram-type=' not in svg or 'Syntax Error' in svg:
                raise RuntimeError(f'Invalid renderer output: {source.name}')
            title = html.escape(source.stem.replace('-', ' ').title())
            svg = re.sub(r'(<svg\b[^>]*>)',
                         lambda match: match[1] + f'<title>{title}</title>'
                         '<rect x="0" y="0" width="100%" height="100%" fill="#FFFFFF"/>',
                         svg, count=1)
            target = args.output_dir.resolve() / source.with_suffix('.svg').name if args.output_dir else source.with_suffix('.svg')
            outputs.append((target, svg))
        # Publish only when every selected diagram renders successfully.
        for target, svg in outputs:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(svg, encoding='utf-8')
            print(f'Rendered {target.relative_to(diagrams) if target.is_relative_to(diagrams) else target}')


if __name__ == '__main__':
    main()
