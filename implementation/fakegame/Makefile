all: clean compile install

clean:
	mvn clean 
compile:
	mvn package
install:
	cp -f game-full/target/game-full-1.0-SNAPSHOT.jar $(H2O_FG_LIBS)

