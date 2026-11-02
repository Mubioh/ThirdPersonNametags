#!/usr/bin/env bash
set -uo pipefail

cd "$(dirname "$0")" || exit 1

java_sources="src/main/java"
mixin_config="src/main/resources/thirdpersonnametags.mixins.json"
mixin_package_dir="$java_sources/me/mubioh/thirdpersonnametags/mixin"
errors=0
warnings=0

error() {
    echo "error: $*"
    errors=$((errors + 1))
}

warning() {
    echo "warning: $*"
    warnings=$((warnings + 1))
}

build_log="$(mktemp)"
trap 'rm -f "$build_log"' EXIT

echo "==> Building"
if ! sh ./gradlew clean build --console=plain >"$build_log" 2>&1; then
    error "gradle build failed: $(grep -m1 -E 'error:|FAILURE|What went wrong' "$build_log" || echo 'see gradle output')"
fi
while IFS= read -r line; do
    warning "javac: $line"
done < <(grep -E '\.java:[0-9]+: warning:' "$build_log")

echo "==> Checking jar"
version="$(sed -n 's/^mod_version=//p' gradle.properties)"
base_name="$(sed -n 's/^archives_base_name=//p' gradle.properties)"
jar="build/libs/${base_name}-${version}.jar"
if [[ -z "$version" ]]; then
    error "gradle.properties has no mod_version"
elif [[ ! -f "$jar" ]]; then
    error "$jar was not built"
else
    jar_version="$(unzip -p "$jar" fabric.mod.json 2>/dev/null | sed -n 's/^ *"version": *"\(.*\)",$/\1/p')"
    if [[ "$jar_version" != "$version" ]]; then
        error "fabric.mod.json version '$jar_version' does not match gradle.properties mod_version '$version'"
    fi
fi

echo "==> Checking mixins"
while IFS= read -r mixin; do
    if [[ ! -f "$mixin_package_dir/$mixin.java" ]]; then
        error "mixin $mixin is declared in $mixin_config but has no source file"
    fi
done < <(grep -oE '"[A-Za-z0-9_]+Mixin"' "$mixin_config" | tr -d '"')
for source in "$mixin_package_dir"/*.java; do
    mixin="$(basename "$source" .java)"
    if ! grep -qF "\"$mixin\"" "$mixin_config"; then
        warning "mixin $mixin exists but is not declared in $mixin_config"
    fi
done

echo "==> Checking sources"
while IFS= read -r line; do
    warning "comment: $line"
done < <(grep -rnE '^\s*(//|/\*|\*)' "$java_sources")
while IFS= read -r line; do
    warning "unfinished: $line"
done < <(grep -rnE 'TODO|FIXME|XXX' src)

echo "==> $errors error(s), $warnings warning(s)"
[[ $errors -eq 0 && $warnings -eq 0 ]]
