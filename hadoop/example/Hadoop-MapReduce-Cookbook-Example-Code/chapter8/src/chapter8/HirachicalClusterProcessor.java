package chapter8;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * This class implements the Hierarchical Clustering Algorithm
 * @author Srinath Perera (hemapani@apache.org)
 */

public class HirachicalClusterProcessor {
    List<ClusterPair> pairsSortedByDistance = new ArrayList<ClusterPair>();
    Map<Cluster, List<ClusterPair>> pairsByID = new HashMap<Cluster, List<ClusterPair>>();

    public class ClusterPair {
        Cluster cluster1;
        Cluster cluster2;
        double distance;

        public ClusterPair(Cluster cluster1, Cluster cluster2) {
            this.cluster1 = cluster1;
            this.cluster2 = cluster2;
            distance = cluster1.getCentriod().getDistance(cluster2.getCentriod());
        }

        // //cluster pair
        // @Override
        // public int compareTo(ClusterPair o) {
        // if (distance - o.distance > 0){
        // return +1;
        // }if (distance - o.distance < 0){
        // return -1;
        // }else{
        // return this.hashCode()-o.hashCode();
        // }
        // }
        // @Override
        // public int hashCode() {
        // if(cluster1.hashCode() < cluster2.hashCode()){
        // return new StringBuffer()
        // .append(String.valueOf(cluster1.hashCode()))
        // .append(String.valueOf(cluster2.hashCode())).hashCode();
        // }else{
        // return new StringBuffer()
        // .append(String.valueOf(cluster2.hashCode()))
        // .append(String.valueOf(cluster1.hashCode())).hashCode();
        // }
        // }
        public Cluster merge() {
            return cluster1.merge(cluster2);
        }

        @Override
        public String toString() {
            return new StringBuffer().append(cluster1).append(",").append(cluster2).append("=").append(distance)
                    .toString();

        }
        // @Override
        // public boolean equals(Object obj) {
        // return hashCode() == obj.hashCode();
        // }
    }

    public class Cluster {
        private ClusterablePoint centriod;
        private List<Cluster> childClusters = new ArrayList<HirachicalClusterProcessor.Cluster>(2);
        Set<ClusterablePoint> clusterPoints = new HashSet<ClusterablePoint>();

        private Cluster() {
        }

        public Cluster(ClusterablePoint point) {
            clusterPoints.add(point);
            calculateCentriod();
        }

        private ClusterablePoint getCentriod() {
            return centriod;
        }

        private void calculateCentriod() {
            if (clusterPoints.size() == 1) {
                this.centriod = clusterPoints.iterator().next();
            }
            /**
             * Common choices include selecting as the clustroid the point that
             * minimizes: 1. The sum of the distances to the other points in the
             * cluster. 2. The maximum distance to another point in the cluster.
             * 3. The sum of the squares of the distances to the other points in
             * the cluster.
             * 
             * We do #1 here
             */
            ClusterablePoint currentCentriod = null;
            int minDistanceSofar = Integer.MAX_VALUE;

            for (ClusterablePoint point1 : clusterPoints) {
                double distance = 0;
                for (ClusterablePoint point2 : clusterPoints) {
                    if (!point1.equals(point2)) {
                        distance = point1.getDistance(point2);
                    }
                    if (distance > minDistanceSofar) {
                        break;
                    }
                }
                if (distance < minDistanceSofar) {
                    currentCentriod = point1;
                }
            }
            this.centriod = currentCentriod;
            if (currentCentriod == null) {
                throw new RuntimeException("centriod must not be null");
            }
        }

        public Cluster merge(Cluster other) {
            Cluster mergedClusters = new Cluster();
            mergedClusters.clusterPoints.addAll(this.clusterPoints);
            mergedClusters.clusterPoints.addAll(other.clusterPoints);

            mergedClusters.childClusters.add(this);
            mergedClusters.childClusters.add(other);
            mergedClusters.calculateCentriod();
            return mergedClusters;
        }

        public Set<ClusterablePoint> getItemsInCluster() throws Exception {
            // List<ClusterablePoint> list = new ArrayList<ClusterablePoint>();
            //
            // LinkedBlockingQueue<Cluster> clusters2Process = new
            // LinkedBlockingQueue<Cluster>();
            // clusters2Process.add(this);
            // while(!clusters2Process.isEmpty()){
            // Cluster take = clusters2Process.take();
            // list.addAll(take.clusterPoints);
            // clusters2Process.addAll(take.childClusters);
            // }
            // return list;
            return clusterPoints;
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer("[");
            for (ClusterablePoint point : clusterPoints) {
                buffer.append(point.print()).append(" ");
            }
            buffer.append("]");
            return buffer.toString();
        }

    }

    public List<Cluster> doHirachicalClustering(List<ClusterablePoint> points) {
        List<Cluster> clusters = new ArrayList<HirachicalClusterProcessor.Cluster>();
        for (ClusterablePoint point : points) {
            clusters.add(new Cluster(point));
        }

        for (int i = 0; i < clusters.size(); i++) {
            for (int j = (i + 1); j < clusters.size(); j++) {
                ClusterPair clusterPair = new ClusterPair(clusters.get(i), clusters.get(j));
                addNewPairs(clusterPair);
                // System.out.println(i +","+j+" added pair "+ clusterPair +
                // " "+ pairsSortedByDistance.size() );
            }
        }
        // //calcuate all distances once
        // for(Cluster cluster1: clusters){
        // for(Cluster cluster2: clusters){
        // if(!cluster1.equals(cluster2)){
        // ClusterPair clusterPair = new ClusterPair(cluster1, cluster2);
        // if(!pairsSortedByDistance.contains(clusterPair)){
        // addNewPairs(clusterPair);
        // System.out.println("added pair "+ clusterPair + " "+
        // pairsSortedByDistance.size() );
        // }
        // }
        // }
        // }
        System.out.println(clusters);
        // System.out.println(pairsByID.keySet());

        // System.out.println("Start");
        while (clusters.size() > 1) {
            // System.out.println(pairsSortedByDistance);

            // this is the pair that is closest to each other. So we merge them

            ClusterPair nextPair = null;
            double minDist = Double.MAX_VALUE;
            for (ClusterPair pair : pairsSortedByDistance) {
                if (pair.distance < minDist) {
                    nextPair = pair;
                    minDist = pair.distance;
                }
            }
            ClusterPair pair = nextPair;

            // remove this pair from sorted list
            removePairs(pair);
            // remove the cluster 1 from all pairs,
            // remove the cluster 2 from all pairs,

            // if(pair.distance == 5){
            // //if min distance is 5, we do not know anything to merge them. So
            // we return
            // return clusters;
            // }

            // merge the pair and add to pair list
            Cluster newCluster = pair.merge();
            clusters.remove(pair.cluster1);
            clusters.remove(pair.cluster2);

            System.out.println("Cluster Size =" + clusters.size() + ", just merged =" + pair.cluster1 + " "
                    + pair.cluster2 + " distance = " + pair.distance);

            // recalcualte pairs for new cluster
            for (Cluster cluster : clusters) {
                ClusterPair newclusterPair = new ClusterPair(cluster, newCluster);
                addNewPairs(newclusterPair);
            }
            clusters.add(newCluster);
        }

        return clusters;
    }

    public void addNewPairs(ClusterPair pair) {
        List<ClusterPair> list = pairsByID.get(pair.cluster1);
        if (list == null) {
            list = new ArrayList<HirachicalClusterProcessor.ClusterPair>();
            pairsByID.put(pair.cluster1, list);
        }
        list.add(pair);

        list = pairsByID.get(pair.cluster2);
        if (list == null) {
            list = new ArrayList<HirachicalClusterProcessor.ClusterPair>();
            pairsByID.put(pair.cluster2, list);
        }
        list.add(pair);

        pairsSortedByDistance.add(pair);
    }

    public void removePairs(ClusterPair pair) {
        List<ClusterPair> pair2Remove1 = pairsByID.remove(pair.cluster1);
        if (pair2Remove1 == null) {
            throw new RuntimeException("Pair " + pair.cluster1 + " not found ");
        } else {

        }
        List<ClusterPair> pair2Remove2 = pairsByID.remove(pair.cluster2);

        if (pair2Remove2 == null) {
            System.out.println(pairsByID.keySet());
            throw new RuntimeException("Pair " + pair.cluster2 + " not found");
        }

        for (ClusterPair clusterPair : pair2Remove1) {
            pairsSortedByDistance.remove(clusterPair);
        }
        for (ClusterPair clusterPair : pair2Remove2) {
            pairsSortedByDistance.remove(clusterPair);
        }

        pairsSortedByDistance.remove(pair);

        // List<ClusterPair> list = pairsByID.get(pair.cluster1.id);
        // if(list != null){
        // list.remove(pair.cluster1);
        // }
        //
        // list = pairsByID.get(pair.cluster2.id);
        // if(list != null){
        // list = new ArrayList<HirachicalClusterProcessor.ClusterPair>();
        // list.remove(pair.cluster2);
        // }
    }

    public static void main(String[] args) throws Exception {
        int estmatedCount = 190000;
        Random random = new Random();

        String line = null;
        BufferedReader reader = new BufferedReader(new FileReader("target/output9/part-r-00000"), 100 * 1024);

        // BufferedReader reader = new BufferedReader(new
        // FileReader("salessata.data"));
        int count = 0;
        List<ClusterablePoint> points = new ArrayList<ClusterablePoint>();
        while ((line = reader.readLine()) != null) {
            int chance = random.nextInt(estmatedCount);
            if (chance <= 1000) {
                AmazonCustomer customer = new AmazonCustomer(line.replaceAll("[0-9]+\\s+", ""));
                points.add(customer);
                count++;
                if (count == 1000) {
                    break;
                }
            }
        }
        reader.close();

        HirachicalClusterProcessor clusterProcessor = new HirachicalClusterProcessor();
        List<Cluster> clusterHead = clusterProcessor.doHirachicalClustering(points);

        List<Cluster> depthNList = new ArrayList<HirachicalClusterProcessor.Cluster>();
        depthNList.addAll(clusterHead);

        List<Cluster> clusterList = new ArrayList<HirachicalClusterProcessor.Cluster>();

        for (int i = 0; i < 7; i++) {
            List<Cluster> tempList = new ArrayList<HirachicalClusterProcessor.Cluster>();
            for (Cluster cluster : depthNList) {
                if (cluster.childClusters.size() == 0) {
                    clusterList.add(cluster);
                } else {
                    tempList.addAll(cluster.childClusters);
                    // System.out.println(i + "=" +tempList);
                }
            }
            depthNList = tempList;
            // System.out.println(depthNList.size());
        }
        clusterList.addAll(depthNList);

        System.out.println("Printing out " + depthNList.size() + " Clusters");
        FileWriter w1 = new FileWriter("clusters.data");
        FileWriter w2 = new FileWriter("clusters-raw.data");
        BufferedWriter bw1 = new BufferedWriter(w1);
        BufferedWriter bw2 = new BufferedWriter(w2);

        int clusterCount = 0;
        for (Cluster cluster : clusterList) {
            Set<ClusterablePoint> itemsInCluster = cluster.getItemsInCluster();
            System.out.println("ClusterInfo:" + clusterCount + "=" + itemsInCluster.size());
            for (ClusterablePoint point : itemsInCluster) {
                ((AmazonCustomer) point).clusterID = String.valueOf(clusterCount);
                w2.write(point.toString());
                w2.write("|");
            }
            w2.write("\n");
            w1.write("Cluster" + clusterCount + "=");
            w1.write(cluster.getCentriod().toString());
            w1.write("\n");
            clusterCount++;
        }
        bw1.close();
        bw2.close();
    }
}
