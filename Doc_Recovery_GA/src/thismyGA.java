import java.util.*;

public class thismyGA {

    int randomSeed = 0; // random seed

    static int popSize = 100; // population size of each generation
    static int genSize = 200; // how many generations to run GA with
    int eliteSize = (int) (0.08 * popSize); // elite size of the generation
    int kSize = 5; // k-size for k-element tournament selection

    int crossOverType = 0; // type 0 is UOX and type 1 is OX (One Point Crossover)
    double crossOverRate = 1.0; // crossover rate for tournament selected parents
    double mutationRate = 0.0; // mutation rate for children

    static int swappingPos; //  crossover point for One Point Crossover

    /*
    Below is an arraylist that collects the indices at which the elite parents are located

    Note that although we use this ArrayList to keep track of elite parents at each generation,
    we clear it after each new generation to make sure it does not contain previous information
    */
    ArrayList<Integer> eliteIndices = new ArrayList<>(eliteSize);

    // Below is the 2D char array that contains our shredded document
    char[][] shreddedDocument = FitnessCalculator.getShreddedDocument("document1-shredded.txt");

    // initialPop is the initial population
    ArrayList<Integer[]> initialPop = new ArrayList<>();

    public thismyGA(int popSize) {
        swappingPos = new Random(randomSeed).nextInt(3, 14);
        Integer[][] popu = new Integer[popSize][15];
        // random numbers are 0,1,2,3

        // Create initial population
        Random randomGenerator = new Random(randomSeed);
        for (int i = 0; i < popSize; i++) {
            Set<Integer> numbers = new LinkedHashSet<>();
            while (numbers.size() < 15) {
                int random = randomGenerator.nextInt(15);
                numbers.add(random);
            }
            numbers.toArray(popu[i]);
            initialPop.add(numbers.toArray(popu[i]));
        }

        // The following lists are for recording the final generation of crossoverType 0 and crossoverType 1
        ArrayList<Integer[]> genX0 = createNewGen(initialPop);
        ArrayList<Integer[]> genX1 = createNewGen(initialPop);

        // This list contains the best fitness values to the final populations of genX0 and genX1
        ArrayList<Double> genFits = new ArrayList<>();


        System.out.println("Parameters are:");
        System.out.println("Random seed: " + randomSeed + ", Crossover Rate: " + (crossOverRate * 100) + ", Mutation Rate: " + (mutationRate * 100));

        // Displaying the best fitness for initial parent population
        double[] initialBestFitness = getFitness(initialPop);
        Arrays.sort(initialBestFitness);
        System.out.println("This is best fitness for initial population " + initialBestFitness[0]);
        System.out.println("This is average fitness for initial population: " + (Arrays.stream(initialBestFitness).sum() / initialBestFitness.length) + "\n");

        // For printing the best and average fitness values for crossoverType 0 for every generation
        System.out.println("For Crossover " + (crossOverType) + "\n");
        for (int i = 0; i < genSize; i++) {
            genX0 = createNewGen(genX0);
            double[] bestfitness = getFitness(genX0);
            Arrays.sort(bestfitness);
            System.out.println("This is best fitness for gen " + (i + 1) + ":\t" + bestfitness[0]);
            System.out.println("This is average fitness for gen " + (i + 1) + ":\t" + (Arrays.stream(bestfitness).sum() / bestfitness.length) + "\n");
            if (i == (genSize - 1)) genFits.add(bestfitness[0]);
        }

        // changing the crossover type to one for it's results

        // For printing the best and average fitness values for crossoverType 0 for every generation
        System.out.println("For Crossover " + (crossOverType) + "\n");
        for (int i = 0; i < genSize; i++) {
            genX1 = createNewGen(genX1);
            double[] bestfitness = getFitness(genX1);
            Arrays.sort(bestfitness);
            System.out.println("This is best fitness for gen " + (i + 1) + ":\t" + bestfitness[0]);
            System.out.println("This is average fitness for gen " + (i + 1) + ":\t" + (Arrays.stream(bestfitness).sum() / bestfitness.length) + "\n");
            if (i == (genSize - 1)) genFits.add(bestfitness[0]);
        }

        // The following is to display the best solution fitness and its corresponding chromosome
        if (genFits.get(0) < genFits.get(1)) {
            System.out.println("Best solution fitness is:\t" + genFits.get(0));
            System.out.println("Best solution chromosome is:\t" + Arrays.deepToString(genX0.get(genSize - 1)).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
        } else {
            System.out.println("Best solution fitness is:\t" + genFits.get(1));
            System.out.println("Best solution chromosome is:\t" + Arrays.deepToString(genX1.get(genSize - 1)).replace("], ", "]\n").replace("[[", "[").replace("]]", "]"));
        }
    }

    /* This method return the fitness values of the given population in order of their chromosome order */
    public double[] getFitness(ArrayList<Integer[]> pop) {

        double[] fitnessArray = new double[popSize];
        for (int i = 0; i < popSize; i++) {
            assert shreddedDocument != null;
            int[] perm = Arrays.stream(pop.get(i)).mapToInt(Integer::intValue).toArray();

            fitnessArray[i] = FitnessCalculator.fitness(shreddedDocument, perm);
        }
        Arrays.sort(fitnessArray);
        return fitnessArray;
    }

    /* This method accepts parent population and returns new children generation */
    public ArrayList<Integer[]> createNewGen(ArrayList<Integer[]> pop) {
//          Make a children array
        /*
        Step 1. Get population

        Step 2:
        select elites
        k-tournament selection
        Add elites to children
        add the returned crossover for tournament selected to children

        Step 3: do mutation
        Step 4: Clear the eliteIndices
         */
        ArrayList<Integer[]> children = new ArrayList<>();
        //getting the elite population
        getElites(pop, eliteSize);

        // mating includes tournament select, crossover, adding elites and returns children
        children = mating(pop, crossOverType);

        // mutation
        children = mutation(children);
        eliteIndices.clear();
        //return children
        return children;
    }

    /* This method makes children from the given parent population */
    public ArrayList<Integer[]> mating(ArrayList<Integer[]> population, int crossOverType) {

        if (crossOverType <= 1) {
            ArrayList<Integer[]> children = new ArrayList<>();

            // tournament selection
            int[] tourSelected = tournamentSelection(population, kSize, popSize, eliteSize);

            // Adding the elite parents to the next generation
            for (int i = 0; i < eliteSize; i++) {
                children.add(population.get(eliteIndices.get(i)));
            }

            // crossOver happens here for the tournament selected parents
            for (int i = 0; i < tourSelected.length; i = i + 2) {

                Integer[] parent1 = population.get(tourSelected[i]);
                Integer[] parent2 = population.get(tourSelected[i + 1]);

                // adding parent 1 and parent 2 as is in case crossover does not happen
                ArrayList<Integer[]> newGen = new ArrayList<>();
                newGen.add(parent1);
                newGen.add(parent2);

                //empirical crossover
                double rand = new Random().nextDouble(0.0, 1.0);
                if (rand <= crossOverRate) {
                    if (crossOverType == 0) newGen = UOX(parent1, parent2);
                    if (crossOverType == 1) newGen = OPCross(parent1, parent2, swappingPos);
                }

                // adding the elements of newGen in the children list
                children.add(newGen.get(0));
                children.add(newGen.get(1));
            }

            return children;
        } else {
            System.out.println("CrossoverType can either be 0 or 1");
            return null;
        }
    }

    /* This method finds the elite parent indices and stores them in eliteIndices ArrayList*/
    public void getElites(ArrayList<Integer[]> pop, int eliteSize) {
        for (int i = 0; i < eliteSize; i++) {
            int fittestIndex = -1;

            for (int j = 0; j < pop.size(); j++) {
                // checking if the index is already in eliteIndices
                if (!eliteIndices.contains(j)) {

                    // j-th index is automatically the fittest if the fittestIndex value is still -1
                    if (fittestIndex == -1) {
                        fittestIndex = j;
                    }

                    // else we compare the fitness of chromosome at the j-th index with the fittestIndex
                    else {
                        int[] perm1 = Arrays.stream(pop.get(j)).mapToInt(Integer::intValue).toArray();
                        int[] perm2 = Arrays.stream(pop.get(fittestIndex)).mapToInt(Integer::intValue).toArray();

                        if (FitnessCalculator.fitness(shreddedDocument, perm2) > FitnessCalculator.fitness(shreddedDocument, perm1)) {
                            fittestIndex = j;
                        }
                    }
                }
            }
            eliteIndices.add(fittestIndex);
        }
//                System.out.println("Initial Elites:\n"+ Arrays.toString(eliteIndices.toArray()));
    }

    /* This is my mutation method. It swaps the values of 2 random indices in a child's chromosome*/
    public ArrayList<Integer[]> mutation(ArrayList<Integer[]> children) {

        for (int i = 0; i < children.size(); i++) {

            // empirically implementing mutation for each child in our children list
            double rand = new Random().nextDouble(0.0, 1.0);
            if (rand <= mutationRate) {
                //generating
//                    int randIndex = new Random().nextInt(0, children.size());


                int randInt1 = new Random().nextInt(0, children.get(i).length);
                int randInt2 = new Random().nextInt(0, children.get(i).length);

//                    System.out.println("Before mutation:" + Arrays.toString(children.get(randIndex)));
                Integer[] child = children.get(i);
                int temp = child[randInt1];

                // Swapping the values
                child[randInt1] = child[randInt2];
                child[randInt2] = temp;
                children.set(i, child);
//                    System.out.println("After mutation:" + Arrays.toString(children.get(randIndex)));
            }
        }
        return children;
    }

    /* This method is for implementing uniform order crossover */
    public ArrayList<Integer[]> UOX(Integer[] parent1, Integer[] parent2) {
        ArrayList<Integer[]> children = new ArrayList<>();

        // generating bitmask with length of a chromosome, 15 in our case. And setting each value to -1 by default
        int[] bitmask = new int[parent1.length];
        Arrays.fill(bitmask, -1);

        // Making 2 Arraylist for 2 children and adding -1 15 times by default. -1 acts as a flag later on
        ArrayList<Integer> child1 = new ArrayList<>();
        ArrayList<Integer> child2 = new ArrayList<>();

        for (int i = 0; i < parent1.length; i++) {
            child1.add(-1);
            child2.add(-1);
        }

        // Initializing the bitmask with values with either 0 or 1
        for (int i = 0; i < bitmask.length; i++) {
            bitmask[i] = new Random().nextInt(0, 2);
        }

        // setting the child Indices with corresponding parent's values if bitmask value is 1
        for (int i = 0; i < bitmask.length; i++) {
            if (bitmask[i] == 1) {
                child1.set(i, parent1[i]);
                child2.set(i, parent2[i]);
            }
        }

        // setting the child Indices with opposite parent's values if bitmask value is 0
        for (int i = 0; i < bitmask.length; i++) {
            if (bitmask[i] == 0) {

                // for child 1
                for (Integer integer : parent2) {
                    // if() condition making sure the child does not already contain the other parent's value i.e. NO DUPLICATES
                    if (!child1.contains(integer)) {
                        child1.set(i, integer);

                        break; // if we find the value in parent 2 that child 1 doesn't have
                    }
                }

                // for child 2
                for (Integer integer : parent1) {

                    // if() condition making sure the child does not already contain the other parent's value i.e. NO DUPLICATES
                    if (!child2.contains(integer)) {
                        child2.set(i, integer);
                        break; // if we find the value in parent 1 that child 2 doesn't have
                    }
                }
            }

        }

        Integer[] child_1 = new Integer[child1.size()];
        child_1 = child1.toArray(child_1);

        Integer[] child_2 = new Integer[child2.size()];
        child_2 = child2.toArray(child_2);

        children.add(child_1);
        children.add(child_2);
        return children;
    }


    /* The method below implements a One Point Crossover i.e. it swaps the elements of the 2 parents after
     * an index that is the Swapping Index */
    public ArrayList<Integer[]> OPCross(Integer[] parent1, Integer[] parent2, int pos) {

        // Making copies of each parent and adding the respective alleles
        // Method will modify the copies first and then assign them to child 1 and child 2
        ArrayList<Integer> parent1_copy = new ArrayList<>();
        ArrayList<Integer> parent2_copy = new ArrayList<>();

        // initialising each entry in parent copy with -1
        for (int i = 0; i < parent1.length; i++) {
            parent1_copy.add(-1);
            parent2_copy.add(-1);
        }
        // setting the parent entries to the copies
        for (int i = 0; i < pos; i++) {
            parent1_copy.set(i, parent1[i]);
            parent2_copy.set(i, parent2[i]);
        }

        // An Arraylist that will contain the resulting 2 children
        ArrayList<Integer[]> children = new ArrayList<>();

        // 2 individual children here
        ArrayList<Integer> child1_allele = new ArrayList<>();
        ArrayList<Integer> child2_allele = new ArrayList<>();

        for (int i = 0; i < parent1_copy.size(); i++) {

            // here we will check if parent1 copy already contains the parent2's value at 'i' th index
            if (!parent1_copy.contains(parent2[i])) child1_allele.add(parent2[i]);
            // here we will check if parent2 copy already contains the parent1's value at 'i' th index
            if (!parent2_copy.contains(parent1[i])) child2_allele.add(parent1[i]);
        }


        for (int i = pos; i < parent1_copy.size(); i++) {
            if (parent1_copy.get(i) == -1) parent1_copy.set(i, child1_allele.remove(0));
        }

        for (int i = pos; i < parent2_copy.size(); i++) {
            if (parent2_copy.get(i) == -1) parent2_copy.set(i, child2_allele.remove(0));
        }

        Integer[] child1 = new Integer[parent1_copy.size()];
        child1 = parent1_copy.toArray(child1);
        Integer[] child2 = new Integer[parent2_copy.size()];
        child2 = parent2_copy.toArray(child2);

        // adding child1 and child 2 to children list
        children.add(child1);
        children.add(child2);

        // returning the list of children
        return children;
    }

    public int[] tournamentSelection(ArrayList<Integer[]> population, int k, int popSize, int eliteSize) {
        Random randomGenerator = new Random(0);
        // initialize an array that will contain indices of the tournament selected parents of size Total population - elite parent size
        int[] tourSelected = new int[popSize - eliteSize];

        // for loop that compares 'k' random parents (Total population - elite parent size) times
        for (int i = 0; i < popSize - eliteSize; i++) {

            // -1 as a flag
            int bestChromInd = -1;

            // function for comapring the fitness of 'k' random parents
            for (int j = 0; j < k; j++) {
                int rand = randomGenerator.nextInt(0, popSize - 1);
                int[] perm = Arrays.stream(population.get(rand)).mapToInt(Integer::intValue).toArray();

                if (bestChromInd == -1 || FitnessCalculator.fitness(shreddedDocument, perm) < FitnessCalculator.fitness(shreddedDocument, Arrays.stream(population.get(bestChromInd)).mapToInt(Integer::intValue).toArray())) {
                    bestChromInd = rand;
                }
            }

            // adding the best parent index found amongst the k randomly selected parents
            tourSelected[i] = bestChromInd;
        }

        //returning the tournament selected array
        return tourSelected;
    }

    public static void main(String[] args) {

        // running the code here :)
        new thismyGA(popSize);

    }
}