rm -r backend/src/main/resources/public
mvn install
rm -r ../c1-vue-compiled/src
cp -r backend/src/ ../c1mastercompiled
cp -r LICENSE.md ../c1mastercompiled
