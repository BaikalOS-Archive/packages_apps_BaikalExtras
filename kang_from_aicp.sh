#!/bin/bash
# BaikalOS Kang script
# (c) BaikalOS 2018 !!!
echo "Welcome to kang script!"
echo "Looking for mosimchah ..."
sleep 1
echo "Mosimchah was not found nearby"
echo "Starting kang!"
find ./ -type f -not -path "./.git/*" -not -path "./kang_from_aicp.sh" -exec sed -i 's/com\.aicp/ru\.baikalos/g' {} \;
find ./ -type f -not -path "./.git/*" -not -path "./kang_from_aicp.sh" -exec sed -i 's/aicp/baikalos/g' {} \;
find ./ -type f -not -path "./.git/*" -not -path "./kang_from_aicp.sh" -exec sed -i 's/Aicp/BaikalOS/g' {} \;
find ./ -type f -not -path "./.git/*" -not -path "./kang_from_aicp.sh" -exec sed -i 's/AICP/BAIKALOS/g' {} \;

# We are decent kangers!
find ./ -type f -not -path "./.git/*" -not -path "./kang_from_aicp.sh" -exec sed -i 's/2017 BAIKALOS/2017 AICP/g' {} \;
find ./ -type f -not -path "./.git/*" -not -path "./kang_from_aicp.sh" -exec sed -i 's/2018 BAIKALOS/2018 AICP/g' {} \;

mkdir src/ru 2> /dev/null
mkdir src/ru/baikalos 2> /dev/null
mv src/com/aicp/extras src/ru/baikalos 2>/dev/null
rm -fr src/com 2> /dev/null
echo "Kang is complete, Sir!"
echo "Poor mosimchah... :'("
