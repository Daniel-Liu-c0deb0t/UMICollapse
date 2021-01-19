mkdir bin
javac --release 11 -cp "lib/htsjdk-2.19.0.jar:lib/snappy-java-1.1.7.3.jar" -d bin src/umicollapse/*/*.java src/test/*.java
cd bin
jar -c -m ../Manifest.txt -f ../umicollapse.jar umicollapse/*/*.class test/*.class
echo "Done!"
