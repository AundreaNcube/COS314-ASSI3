# COS314 Assignment 3 -- Genetic Programming Classifiers

## Introduction

This project implements two Genetic Programming (GP) classifiers to classify the
**Breast Cancer Wisconsin (Diagnostic) Dataset**:
1. **Arithmetic Classifier** - evolves symbolic arithmetic expressions. The numeric output
   of the evolved tree is thresholded (output ≥ 0 -> class 1, output < 0 -> class 0).

2. **Decision Tree Classifier** - evolves decision trees using logical and comparison
   operators (AND, OR, NOT, IF, <=, >). The tree output is interpreted as a binary class.

Both classifiers share a common GP engine (tree representation, population management,
selection, crossover, point mutation) and a utility layer (data loading, metrics,
statistical significance testing).

---

## Team Members

| Name | Student Number |
|------|---------------|
| [Amantle Temo -- Decision Tree & Utilities] | [u23539764] |
| [Aundrea Ncube -- Arithmetic Classifier] | [u22747363] |
| [Member 3 -- GP Engine] | [u23534975] |

---

## File Structure

```
COS314-ASSI3/
├── data/
│   ├── Breast_train.csv        # Training dataset (183 instances)
│   └── Breast_test.csv         # Test dataset (86 instances)
├── src/
│   ├── util/
│   │   ├── DataLoader.java     # CSV loading (shared)
│   │   ├── Metrics.java        # Accuracy + F-measure (shared)
│   │   └── StatTests.java      # T-test / Wilcoxon (shared)
│   └── gp/
│       ├── engine/
│       │   ├── Node.java       # GP tree node
│       │   ├── Tree.java       # GP tree + S-expression serialisation
│       │   ├── Population.java # Population management
│       │   ├── Selection.java  # Tournament selection
│       │   ├── Crossover.java  # Subtree crossover
│       │   └── Mutation.java   # Point mutation
│       ├── decisiontree/
│       │   ├── DTFunctionSet.java   # DT operator + terminal definitions
│       │   ├── DTClassifier.java    # Wraps a Tree for DT classification
│       │   ├── DTTrainer.java       # 30-run GP training loop (main)
│       │   └── DTTester.java        # Load model + classify test set (main)
│       └── arithmetic/
│           ├── ArithFunctionSet.java
│           ├── ArithmeticClassifier.java
│           ├── ArithTrainer.java
│           └── ArithTester.java
├── Makefile
└── README.md
```

---

## Prerequisites

- **Java JDK **
- **make** (GNU Make or compatible)

---

## How to Build

### Build everything (all four JARs)
```bash
make all
```

### Build only the Decision Tree JARs (+ shared util/engine)
```bash
make dt
```

### Build only the shared utility classes
```bash
make util
```

### Clean all compiled files and JARs
```bash
make clean
```

---

## How to Run

### Decision Tree — Training
```bash
make run-dt-train
```
or directly:
```bash
java -jar DTTrainer.jar
```

You will be prompted for:
```
Enter seed:
Enter training data file path:
Enter crossover probability:
Enter mutation probability:
Enter tournament size:
Enter max initial tree depth:
Enter max offspring depth:
Enter max mutation offspring depth:
```

**Example inputs:**
```
42
data/Breast_train.csv
0.9
0.1
5
6
8
6
```

The trainer runs **30 independent runs × 100 generations** and saves the best evolved
tree to `best_dt_model.txt`.

---

### Decision Tree — Testing
```bash
make run-dt-test
```
or directly:
```bash
java -jar DTTester.jar
```

You will be prompted for:
```
Enter path to saved model file :
Enter path to test data CSV:
```

**Example inputs:**
```
best_dt_model.txt
data/Breast_test.csv
```

Output includes test accuracy, F-measure, and runtime.

---

### Arithmetic Classifier — Training
```bash
make run-arith-train
# or: java -jar ArithTrainer.jar
```

### Arithmetic Classifier — Testing
```bash
make run-arith-test
# or: java -jar ArithTester.jar
```

---

## Reproducibility

- All runs are seeded. The seed for run *i* (0-indexed) is `base_seed + i`.
- To replicate the best result from the demo, use the reported seed value with the
  same parameter settings.

---

## GP Parameters (Design Decisions)

| Parameter | Value |
|-----------|-------|
| Population size | 200 |
| Initial tree generation | Ramped half-and-half |
| Selection method | Tournament selection |
| Mutation type | Point mutation |
| Fitness function | Classification accuracy |
| Maximum generations | 100 |
| Independent runs | 30 |

Crossover rate, mutation rate, tournament size, and depth limits are entered at runtime.

---

## Dataset

The Breast Cancer Wisconsin dataset is pre-encoded as integers:

| Feature | Encoding |
|---------|----------|
| Class | no-recurrence=0, recurrence=1 |
| Age | 20–29=0, 30–39=1, 40–49=2, 50–59=3, 60–69=4, 70–79=5 |
| Menopause | premeno=0, ge40=1, lt40=2 |
| Node-Caps / Irradiat | no=0, yes=1, ?=2 |
| Breast | left=0, right=1 |
| Quad | left_low=0, right_up=1, left_up=2, right_low=3, central=4, ?=5 |
