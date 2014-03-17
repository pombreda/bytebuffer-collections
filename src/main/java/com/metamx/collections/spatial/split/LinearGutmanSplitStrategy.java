package com.metamx.collections.spatial.split;

import com.metamx.collections.spatial.Node;

import java.util.List;

/**
 */
public class LinearGutmanSplitStrategy extends GutmanSplitStrategy
{
  public LinearGutmanSplitStrategy(int minNumChildren, int maxNumChildren)
  {
    super(minNumChildren, maxNumChildren);
  }

  /**
   * This algorithm is from the original paper.
   *
   * Algorithm LinearPickSeeds. Select two entries to be the first elements of the groups.
   *
   * LPS1. [Find extreme rectangles along all dimensions]. Along each dimension, find the entry whose rectangle has
   * the highest low side, and the one with the lowest high side. Record the separation.
   *
   * LPS2. [Adjust for shape of the rectangle cluster]. Normalize the separations by dividing by the width of the
   * entire set along the corresponding dimension.
   *
   * LPS3. [Select the most extreme pair]. Choose the pair with the greatest normalized separation along any dimension.
   *
   * @param nodes - nodes to choose from
   * @return - two groups representing the seeds
   */
  @Override
  public Node[] pickSeeds(List<Node> nodes)
  {
    int[] optimalIndices = new int[2];
    int numDims = nodes.get(0).getNumDims();

    double bestNormalized = 0.0;
    for (int i = 0; i < numDims; i++) {
      float minCoord = Float.MAX_VALUE;
      float maxCoord = -Float.MAX_VALUE;
      float highestLowSide = -Float.MAX_VALUE;
      float lowestHighside = Float.MAX_VALUE;
      int highestLowSideIndex = 0;
      int lowestHighSideIndex = 0;

      int counter = 0;
      for (Node node : nodes) {
        minCoord = Math.min(minCoord, node.getMinCoordinates()[i]);
        maxCoord = Math.max(maxCoord, node.getMaxCoordinates()[i]);

        if (node.getMinCoordinates()[i] > highestLowSide) {
          highestLowSide = node.getMinCoordinates()[i];
          highestLowSideIndex = counter;
        }
        if (node.getMaxCoordinates()[i] < lowestHighside) {
          lowestHighside = node.getMaxCoordinates()[i];
          lowestHighSideIndex = counter;
        }

        counter++;
      }
      double normalizedSeparation = (highestLowSideIndex == lowestHighSideIndex) ? -1.0 :
                          Math.abs((highestLowSide - lowestHighside) / (maxCoord - minCoord));
      if (normalizedSeparation > bestNormalized) {
        optimalIndices[0] = highestLowSideIndex;
        optimalIndices[1] = lowestHighSideIndex;
        bestNormalized = normalizedSeparation;
      }
    }

    // Didn't actually find anything, just return first 2 children
    if (bestNormalized == 0) {
      optimalIndices[0] = 0;
      optimalIndices[1] = 1;
    }

    int indexToRemove1 = Math.min(optimalIndices[0], optimalIndices[1]);
    int indexToRemove2 = Math.max(optimalIndices[0], optimalIndices[1]);
    return new Node[]{nodes.remove(indexToRemove1), nodes.remove(indexToRemove2 - 1)};
  }

  /**
   * This algorithm is from the original paper.
   *
   * Algorithm LinearPickNext. PickNext simply choose any of the remaining entries.
   *
   * @param nodes - remaining nodes
   * @param groups - the left and right groups
   * @return - the optimal selected node
   */
  @Override
  public Node pickNext(List<Node> nodes, Node[] groups)
  {
    return nodes.remove(0);
  }
}