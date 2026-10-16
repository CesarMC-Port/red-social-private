#!/usr/bin/env bash
#
# Sincroniza el estado actual de RED-SOCIAL con el repo privado de producción
# (carpeta .private-red-social/, gitignoreada). Copia tracked + untracked-no-ignorado,
# excluyendo docs/ y la propia carpeta, y luego commitea y pushea el mirror.
#
# La URL y la identidad del repo privado viven SOLO en
# .private-red-social/.git/config (gitignoreado) y NUNCA en el repo público.
set -euo pipefail

REPO_ROOT="$(git rev-parse --show-toplevel)"
MIRROR="$REPO_ROOT/.private-red-social"

if [ ! -d "$MIRROR/.git" ]; then
  echo "[sync-private] No existe el repo privado en $MIRROR; nada que sincronizar." >&2
  exit 0
fi

# 1) Vaciar el mirror salvo su .git
find "$MIRROR" -mindepth 1 -maxdepth 1 ! -name '.git' -exec rm -rf {} +

# 2) Copiar tracked + untracked-no-ignorado, excluyendo docs/ y el mirror
FILES="$(git -C "$REPO_ROOT" ls-files --cached --others --exclude-standard \
          | grep -v -e '^docs/' -e '^\.private-red-social/' || true)"
while IFS= read -r f; do
  [ -z "$f" ] && continue
  mkdir -p "$MIRROR/$(dirname "$f")"
  cp -a "$REPO_ROOT/$f" "$MIRROR/$f"
done <<< "$FILES"

# 3) Commit + push en el repo privado
cd "$MIRROR"
git add -A
if ! git diff --cached --quiet; then
  git commit -m "sync: ${REPO_ROOT##*/} $(git -C "$REPO_ROOT" rev-parse --short HEAD)"
fi
git push origin main
