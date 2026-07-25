#!/usr/bin/env bash
set -euo pipefail

MINIMUM_VERSION="${1:-1.20.5}"
USER_AGENT="ConfigStackMC-release-bot/2.0 (https://github.com/TheETR/ConfigStackMC)"
PAPER_PROJECT="paper"
OUTPUT_FILE="${GITHUB_OUTPUT:-/dev/stdout}"

require_tool() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required tool: $1" >&2
    exit 1
  fi
}

for tool in curl jq sort grep sed awk; do
  require_tool "$tool"
done

is_release_version() {
  [[ "$1" =~ ^[0-9]+(\.[0-9]+){1,2}$ ]]
}

version_at_least() {
  local version="$1"
  local minimum="$2"
  [[ "$(printf '%s\n%s\n' "$minimum" "$version" | sort -V | head -n 1)" == "$minimum" ]]
}

paper_versions() {
  curl -fsSL -H "User-Agent: ${USER_AGENT}" \
    "https://fill.papermc.io/v3/projects/${PAPER_PROJECT}" \
    | jq -r '.versions | to_entries[] | .value[]' \
    | grep -E '^[0-9]+(\.[0-9]+){1,2}$' \
    | sort -Vu
}

latest_paper_build() {
  local minecraft_version="$1"
  curl -fsSL -H "User-Agent: ${USER_AGENT}" \
    "https://fill.papermc.io/v3/projects/${PAPER_PROJECT}/versions/${minecraft_version}/builds" \
    | jq -c 'if type == "array" then (max_by(.id) // empty) else empty end'
}

FABRIC_API_METADATA="$({
  curl -fsSL "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml"
})"

latest_fabric_api_for_mc() {
  local minecraft_version="$1"
  local suffix="+${minecraft_version}"

  printf '%s' "$FABRIC_API_METADATA" \
    | grep -o '<version>[^<]*</version>' \
    | sed -E 's#</?version>##g' \
    | awk -v suffix="$suffix" 'substr($0, length($0) - length(suffix) + 1) == suffix' \
    | sort -V \
    | tail -n 1 || true
}

FABRIC_LOADER_JSON="$(curl -fsSL 'https://meta.fabricmc.net/v2/versions/loader')"
FABRIC_LOADER_VERSION="$(jq -r 'first(.[]).version // empty' <<<"$FABRIC_LOADER_JSON")"
FABRIC_LOADER_STABLE="$(jq -r 'first(.[]).stable // false' <<<"$FABRIC_LOADER_JSON")"

if [[ -z "$FABRIC_LOADER_VERSION" ]]; then
  echo "No Fabric Loader version could be resolved." >&2
  exit 1
fi

PAPER_TARGETS='[]'
FABRIC_TARGETS='[]'

while IFS= read -r minecraft_version; do
  [[ -n "$minecraft_version" ]] || continue
  is_release_version "$minecraft_version" || continue
  version_at_least "$minecraft_version" "$MINIMUM_VERSION" || continue

  paper_build_json="$(latest_paper_build "$minecraft_version" || true)"
  [[ -n "$paper_build_json" ]] || continue

  paper_build="$(jq -r '.id' <<<"$paper_build_json")"
  paper_channel="$(jq -r '.channel // "UNKNOWN"' <<<"$paper_build_json")"

  PAPER_TARGETS="$(jq -c \
    --arg minecraft_version "$minecraft_version" \
    --arg paper_build "$paper_build" \
    --arg paper_channel "$paper_channel" \
    '. + [{minecraft_version: $minecraft_version, paper_build: $paper_build, paper_channel: $paper_channel}]' \
    <<<"$PAPER_TARGETS")"

  fabric_api_version="$(latest_fabric_api_for_mc "$minecraft_version")"
  if [[ -z "$fabric_api_version" ]]; then
    echo "No Fabric API artifact for Minecraft ${minecraft_version}; Paper will still be packaged." >&2
    continue
  fi

  if version_at_least "$minecraft_version" '26.1'; then
    fabric_project='fabric'
    java_version='25'
    loom_generation='unobfuscated'
  else
    fabric_project='fabric-legacy'
    java_version='21'
    loom_generation='remapped'
  fi

  FABRIC_TARGETS="$(jq -c \
    --arg minecraft_version "$minecraft_version" \
    --arg paper_build "$paper_build" \
    --arg paper_channel "$paper_channel" \
    --arg fabric_api_version "$fabric_api_version" \
    --arg fabric_loader_version "$FABRIC_LOADER_VERSION" \
    --arg fabric_loader_stable "$FABRIC_LOADER_STABLE" \
    --arg fabric_project "$fabric_project" \
    --arg java_version "$java_version" \
    --arg loom_generation "$loom_generation" \
    '. + [{
      minecraft_version: $minecraft_version,
      paper_build: $paper_build,
      paper_channel: $paper_channel,
      fabric_api_version: $fabric_api_version,
      fabric_loader_version: $fabric_loader_version,
      fabric_loader_stable: ($fabric_loader_stable == "true"),
      fabric_project: $fabric_project,
      java_version: $java_version,
      loom_generation: $loom_generation
    }]' <<<"$FABRIC_TARGETS")"
done < <(paper_versions)

paper_count="$(jq 'length' <<<"$PAPER_TARGETS")"
fabric_count="$(jq 'length' <<<"$FABRIC_TARGETS")"

if [[ "$paper_count" -eq 0 ]]; then
  echo "No Paper targets were discovered at or after Minecraft ${MINIMUM_VERSION}." >&2
  exit 1
fi

latest_target="$(jq -r 'last' <<<"$PAPER_TARGETS")"
latest_minecraft_version="$(jq -r '.minecraft_version' <<<"$latest_target")"
latest_paper_build="$(jq -r '.paper_build' <<<"$latest_target")"
latest_paper_channel="$(jq -r '.paper_channel' <<<"$latest_target")"

if [[ "$latest_paper_channel" == 'STABLE' ]]; then
  auto_prerelease='false'
else
  auto_prerelease='true'
fi

{
  echo "paper_targets=$(jq -c '.' <<<"$PAPER_TARGETS")"
  echo "fabric_matrix=$(jq -c '{include: .}' <<<"$FABRIC_TARGETS")"
  echo "paper_target_count=${paper_count}"
  echo "fabric_target_count=${fabric_count}"
  echo "latest_minecraft_version=${latest_minecraft_version}"
  echo "latest_paper_build=${latest_paper_build}"
  echo "latest_paper_channel=${latest_paper_channel}"
  echo "fabric_loader_version=${FABRIC_LOADER_VERSION}"
  echo "fabric_loader_stable=${FABRIC_LOADER_STABLE}"
  echo "auto_prerelease=${auto_prerelease}"
} >>"$OUTPUT_FILE"

cat <<EOF_SUMMARY
Resolved ConfigStackMC release matrix:
  Minimum Minecraft: ${MINIMUM_VERSION}
  Latest Minecraft: ${latest_minecraft_version}
  Latest Paper build: ${latest_paper_build} (${latest_paper_channel})
  Paper aliases: ${paper_count}
  Fabric targets: ${fabric_count}
  Fabric Loader: ${FABRIC_LOADER_VERSION} (stable=${FABRIC_LOADER_STABLE})
EOF_SUMMARY
