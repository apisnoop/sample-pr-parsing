CONFORMANCE_URL=https://raw.githubusercontent.com/kubernetes/kubernetes/release-1.18/test/conformance/testdata/conformance.yaml
curl $CONFORMANCE_URL |
    # yq reads stdin(-) and outputs as json('-j')
    yq read -j - |
    # jq reads json array and maps over grabbing just the codename
    jq '.[] | .codename' \
    > conformance-tests.json
echo "test codenames written to conformance-tests.json"
