package dev.lounres.raytracingCourse.raytracing

import dev.lounres.raytracingCourse.raytracing.BVHImpl.Node
import dev.lounres.raytracingCourse.raytracing.figure.AABB
import dev.lounres.raytracingCourse.raytracing.figure.Figure
import dev.lounres.raytracingCourse.raytracing.figure.FiniteFigure
import dev.lounres.raytracingCourse.raytracing.figure.and
import dev.lounres.raytracingCourse.raytracing.figure.intersects
import dev.lounres.raytracingCourse.raytracing.geometry.Ray


internal interface BVH {
    val root: Node
    fun intersect(ray: Ray, fromSceneObject: SceneObject<Figure>?): Intersection?
    
    interface Node {
        val aabb: AABB
        val sceneObjects: List<SceneObject<FiniteFigure>>
        val leftChild: Node?
        val rightChild: Node?
    }
}

internal fun BVH.Node.cost(): Double = sceneObjects.size * aabb.surfaceArea

internal fun constructNode(sceneObjects: List<SceneObject<FiniteFigure>>): Node {
    val simpleNode = Node(
        sceneObjects = sceneObjects
    )
    
    if (sceneObjects.size <= 4) return simpleNode
    
    var minCost = simpleNode.aabb.surfaceArea * simpleNode.sceneObjects.size
    var pairOfParts: Pair<List<SceneObject<FiniteFigure>>, List<SceneObject<FiniteFigure>>>? = null
    
    val xSortedFigures = sceneObjects.sortedBy { it.figure.AABB.center.x }
    for (i in 1 ..< xSortedFigures.size) {
        val firstHalfAABB = xSortedFigures.take(i).map { it.figure.AABB }.reduce { a, b -> a and b }
        val secondHalfAABB = xSortedFigures.drop(i).map { it.figure.AABB }.reduce { a, b -> a and b }
        val cost = firstHalfAABB.surfaceArea * i + secondHalfAABB.surfaceArea * (xSortedFigures.size - i)
        if (minCost > cost) {
            pairOfParts = Pair(xSortedFigures.subList(0, i), xSortedFigures.subList(i, xSortedFigures.size))
            minCost = cost
        }
    }
    
    val ySortedFigures = sceneObjects.sortedBy { it.figure.AABB.center.y }
    for (i in 1 ..< ySortedFigures.size) {
        val firstHalfAABB = ySortedFigures.take(i).map { it.figure.AABB }.reduce { a, b -> a and b }
        val secondHalfAABB = ySortedFigures.drop(i).map { it.figure.AABB }.reduce { a, b -> a and b }
        val cost = firstHalfAABB.surfaceArea * i + secondHalfAABB.surfaceArea * (ySortedFigures.size - i)
        if (minCost > cost) {
            pairOfParts = Pair(ySortedFigures.subList(0, i), ySortedFigures.subList(i, ySortedFigures.size))
            minCost = cost
        }
    }
    
    val zSortedFigures = sceneObjects.sortedBy { it.figure.AABB.center.z }
    for (i in 1 ..< zSortedFigures.size) {
        val firstHalfAABB = zSortedFigures.take(i).map { it.figure.AABB }.reduce { a, b -> a and b }
        val secondHalfAABB = zSortedFigures.drop(i).map { it.figure.AABB }.reduce { a, b -> a and b }
        val cost = firstHalfAABB.surfaceArea * i + secondHalfAABB.surfaceArea * (zSortedFigures.size - i)
        if (minCost > cost) {
            pairOfParts = Pair(zSortedFigures.subList(0, i), zSortedFigures.subList(i, zSortedFigures.size))
            minCost = cost
        }
    }
    
    if (pairOfParts == null) return simpleNode
    
    val leftChild = constructNode(pairOfParts.first)
    val rightChild = constructNode(pairOfParts.second)
    
    val newNode = Node(sceneObjects = emptyList(), aabb = leftChild.aabb and rightChild.aabb)
    newNode.leftChild = leftChild
    newNode.rightChild = rightChild
    
    return newNode
}

internal fun BVH(figures: List<SceneObject<FiniteFigure>>): BVH = BVHImpl(constructNode(figures))

internal class BVHImpl(override val root: Node) : BVH {
    class Node(
        override val sceneObjects: List<SceneObject<FiniteFigure>>,
        override val aabb: AABB = sceneObjects.map { it.figure.AABB }.reduce { a, b -> a and b },
    ) : BVH.Node {
        override var leftChild: Node? = null
        override var rightChild: Node? = null
    }
    
    override fun intersect(ray: Ray, fromSceneObject: SceneObject<Figure>?): Intersection? = root.intersect(ray, fromSceneObject)
    
    internal companion object {
        private fun Node.intersect(ray: Ray, fromSceneObject: SceneObject<Figure>? = null): Intersection? {
            var closestIntersection: Intersection? = null
            for (sceneObject in sceneObjects) {
                val intersectionMoment =
                    if (sceneObject != fromSceneObject) sceneObject.figure.intersect(ray) else sceneObject.figure.intersectAgain(
                        ray
                    )
                if (intersectionMoment != null && (closestIntersection == null || closestIntersection.moment > intersectionMoment))
                    closestIntersection = Intersection(moment = intersectionMoment, sceneObject = sceneObject)
            }
            val leftChild = leftChild
            if (leftChild != null && ray intersects leftChild.aabb) {
                val intersectionMoment = leftChild.intersect(ray, fromSceneObject)
                if (intersectionMoment != null && (closestIntersection == null || closestIntersection.moment > intersectionMoment.moment))
                    closestIntersection = intersectionMoment
            }
            val rightChild = rightChild
            if (rightChild != null && ray intersects rightChild.aabb) {
                val intersectionMoment = rightChild.intersect(ray, fromSceneObject)
                if (intersectionMoment != null && (closestIntersection == null || closestIntersection.moment > intersectionMoment.moment))
                    closestIntersection = intersectionMoment
            }
            return closestIntersection
        }
    }
}