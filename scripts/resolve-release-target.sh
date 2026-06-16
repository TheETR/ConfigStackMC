#!/usr/bin/env bash
set -euo pipefail

REQUESTED_VERSION="${1:-auto}"
USER_AGENT="ConfigStackMC-release-bot/1.0 (https://github.com/TheETR/ConfigStackMC)"
PAPER_PROJECT="paper"

require_tool() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required tool: $1" >&2
    exit 1
  fi
}

require_tool curl
require_tool jq
require_tool sort

paper_versions() {
  curl -fsSL -H "User-Agent: ${USER_AGENT}" "https://fill.papermc.io/v3/projects/${PAPER_PROJECT}" \
    | jq -r '.versions | to_entries[] | .value[]' \
    | sort -V -r
}

latest_stable_paper_build() {
  local mc_version="$1"
  curl -fsSL -H "User-Agent: ${USER_AGENT}" "https://fill.papermc.io/v3/projects/${PAPER_PROJECT}/versions/${mc_version}/builds" \
    | jq -r 'first(.[] | select(.channel == "STABLE") | .id) // empty'
}

latest_stable_fabric_loader() {
  curl -fsSL "https://meta.fabricmc.net/v2/versions/loader" \
    | jq -r 'first(.[] | select(.stable == true) | .version) // empty'
}

latest_fabric_api_for_mc() {
  local mc_version="$1"
  curl -fsSL "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml" \
    | grep -o '<version>[^<]*</version>' \
    | sed -E 's#</?version>##g' \
    | grep -F "+${mc_version}" \
    | sort -V \
    | tail -n 1 || true
}

find_target_version() {
  if [[ "${REQUESTED_VERSION}" != "auto" && -n "${REQUESTED_VERSION}" ]]; then
    echo "${REQUESTED_VERSION}"
    return 0
  fi

  local version
  for version in $(paper_versions); do
    local paper_build fabric_api
    paper_build="$(latest_stable_paper_build "${version}" || true)"
    fabric_api="$(latest_fabric_api_for_mc "${version}" || true)"
    if [[ -n "${paper_build}" && -n "${fabric_api}" ]]; then
      echo "${version}"
      return 0
    fi
  done

  echo "Could not find a Minecraft version that has both a stable Paper build and a matching Fabric API." >&2
  exit 1
}

MC_VERSION="$(find_target_version)"
PAPER_BUILD="$(latest_stable_paper_build "${MC_VERSION}" || true)"
FABRIC_API_VERSION="$(latest_fabric_api_for_mc "${MC_VERSION}" || true)"
FABRIC_LOADER_VERSION="$(latest_stable_fabric_loader || true)"

if [[ -z "${PAPER_BUILD}" ]]; then
  echo "No stable Paper build found for Minecraft ${MC_VERSION}." >&2
  exit 1
fi

if [[ -z "${FABRIC_API_VERSION}" ]]; then
  echo "No Fabric API artifact found for Minecraft ${MC_VERSION}." >&2
  exit 1
fi

if [[ -z "${FABRIC_LOADER_VERSION}" ]]; then
  echo "No stable Fabric Loader version found." >&2
  exit 1
fi

PAPER_API_VERSION="${MC_VERSION}.build.${PAPER_BUILD}-stable"
PAPER_API_VERSION_SHORT="$(echo "${MC_VERSION}" | awk -F. '{print $1"."$2}')"

{
  echo "minecraft_version=${MC_VERSION}"
  echo "paper_build=${PAPER_BUILD}"
  echo "paper_api_version=${PAPER_API_VERSION}"
  echo "paper_api_version_short=${PAPER_API_VERSION_SHORT}"
  echo "fabric_loader_version=${FABRIC_LOADER_VERSION}"
  echo "fabric_api_version=${FABRIC_API_VERSION}"
} >> "${GITHUB_OUTPUT:-/dev/stdout}"

cat <<EOF
Resolved ConfigStackMC target:
  Minecraft: ${MC_VERSION}
  Paper API: ${PAPER_API_VERSION}
  Fabric Loader: ${FABRIC_LOADER_VERSION}
  Fabric API: ${FABRIC_API_VERSION}
EOF
