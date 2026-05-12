## ============================================================
## COS314 Assignment 3 — Genetic Programming Classifiers
## Build system: standard JDK (javac + jar), no external tools
## ============================================================

SRC_DIR   = src
BIN_DIR   = bin
JAVAC     = javac
JAR       = jar
JAVA      = java
JAVAC_FLAGS = -cp $(SRC_DIR) -d $(BIN_DIR)

## ---- Source groups ----
UTIL_SRC  = $(SRC_DIR)/util/DataLoader.java \
            $(SRC_DIR)/util/Metrics.java \
            $(SRC_DIR)/util/StatTests.java

ENGINE_SRC = $(SRC_DIR)/gp/engine/Node.java \
             $(SRC_DIR)/gp/engine/Tree.java \
             $(SRC_DIR)/gp/engine/Population.java \
             $(SRC_DIR)/gp/engine/Selection.java \
             $(SRC_DIR)/gp/engine/Crossover.java \
             $(SRC_DIR)/gp/engine/Mutation.java

DT_SRC    = $(SRC_DIR)/gp/decisiontree/DTFunctionSet.java \
            $(SRC_DIR)/gp/decisiontree/DTClassifier.java \
            $(SRC_DIR)/gp/decisiontree/DTTrainer.java \
            $(SRC_DIR)/gp/decisiontree/DTTester.java

ARITH_SRC = $(SRC_DIR)/gp/arithmetic/ArithFunctionSet.java \
            $(SRC_DIR)/gp/arithmetic/ArithmeticClassifier.java \
            $(SRC_DIR)/gp/arithmetic/ArithTrainer.java \
            $(SRC_DIR)/gp/arithmetic/ArithTester.java

ALL_SRC   = $(UTIL_SRC) $(ENGINE_SRC) $(DT_SRC) $(ARITH_SRC)

## ============================================================
## Default target: build everything
## ============================================================
.PHONY: all
all: $(BIN_DIR) compile \
     DTTrainer.jar DTTester.jar \
     ArithTrainer.jar ArithTester.jar
	@echo "Build complete."

## ============================================================
## Compile all sources
## ============================================================
.PHONY: compile
compile: $(BIN_DIR)
	$(JAVAC) $(JAVAC_FLAGS) $(ALL_SRC)

## ============================================================
## Compile only util + engine + DT (your section)
## ============================================================
.PHONY: dt
dt: $(BIN_DIR) compile-dt DTTrainer.jar DTTester.jar
	@echo "DT build complete."

.PHONY: compile-dt
compile-dt: $(BIN_DIR)
	$(JAVAC) $(JAVAC_FLAGS) $(UTIL_SRC) $(ENGINE_SRC) $(DT_SRC)

## ============================================================
## Compile only util (shared library)
## ============================================================
.PHONY: util
util: $(BIN_DIR)
	$(JAVAC) $(JAVAC_FLAGS) $(UTIL_SRC)
	@echo "Util compiled."

## ============================================================
## JAR targets
## ============================================================

## Manifest helper: write a temp manifest then delete it
DTTrainer.jar: compile-dt
	@echo "Main-Class: gp.decisiontree.DTTrainer" > manifest_dt_train.txt
	$(JAR) cfm DTTrainer.jar manifest_dt_train.txt -C $(BIN_DIR) .
	@del manifest_dt_train.txt 2>nul || rm -f manifest_dt_train.txt
	@echo "Created DTTrainer.jar"

DTTester.jar: compile-dt
	@echo "Main-Class: gp.decisiontree.DTTester" > manifest_dt_test.txt
	$(JAR) cfm DTTester.jar manifest_dt_test.txt -C $(BIN_DIR) .
	@del manifest_dt_test.txt 2>nul || rm -f manifest_dt_test.txt
	@echo "Created DTTester.jar"

ArithTrainer.jar: compile
	@echo "Main-Class: gp.arithmetic.ArithTrainer" > manifest_arith_train.txt
	$(JAR) cfm ArithTrainer.jar manifest_arith_train.txt -C $(BIN_DIR) .
	@del manifest_arith_train.txt 2>nul || rm -f manifest_arith_train.txt
	@echo "Created ArithTrainer.jar"

ArithTester.jar: compile
	@echo "Main-Class: gp.arithmetic.ArithTester" > manifest_arith_test.txt
	$(JAR) cfm ArithTester.jar manifest_arith_test.txt -C $(BIN_DIR) .
	@del manifest_arith_test.txt 2>nul || rm -f manifest_arith_test.txt
	@echo "Created ArithTester.jar"

## ============================================================
## Run targets
## ============================================================
.PHONY: run-dt-train
run-dt-train: DTTrainer.jar
	$(JAVA) -jar DTTrainer.jar

.PHONY: run-dt-test
run-dt-test: DTTester.jar
	$(JAVA) -jar DTTester.jar

.PHONY: run-arith-train
run-arith-train: ArithTrainer.jar
	$(JAVA) -jar ArithTrainer.jar

.PHONY: run-arith-test
run-arith-test: ArithTester.jar
	$(JAVA) -jar ArithTester.jar

## ============================================================
## Clean
## ============================================================
.PHONY: clean
clean:
	@rm -rf $(BIN_DIR)
	@rm -f DTTrainer.jar DTTester.jar ArithTrainer.jar ArithTester.jar
	@rm -f best_dt_model.txt best_arith_model.txt
	@rm -f manifest_*.txt
	@echo "Clean complete."

## ============================================================
## Create bin directory
## ============================================================
$(BIN_DIR):
	@mkdir -p $(BIN_DIR)
