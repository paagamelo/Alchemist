/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Segment2DImpl
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Intersection2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import org.junit.jupiter.api.assertThrows
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Creates an [Euclidean2DPosition].
 */
fun coords(x: Double, y: Double) = Euclidean2DPosition(x, y)

/**
 * Creates a [Segment2D].
 */
fun segment(x1: Double, y1: Double, x2: Double, y2: Double) = Segment2DImpl(coords(x1, y1), coords(x2, y2))

class TestSegment2DImpl : StringSpec() {
    private val horizontalSegment: Segment2D<Euclidean2DPosition> = segment(2.0, 2.0, 6.0, 2.0)
    private val verticalSegment: Segment2D<Euclidean2DPosition> = segment(2.0, 2.0, 2.0, 6.0)
    private val obliqueSegment: Segment2D<Euclidean2DPosition> = segment(2.0, 2.0, 6.0, 6.0)
    private val degenerateSegment: Segment2D<Euclidean2DPosition> = segment(2.0, 2.0, 2.0, 2.0)

    private fun <P : Vector2D<P>> segmentsIntersectionShouldBe(
        segment1: Segment2D<P>,
        segment2: Segment2D<P>,
        expectedPoint: P
    ) {
        val intersection = intersectionShouldBe<P, Intersection2D.SinglePoint<P>>(segment1, segment2, false)
        intersection.point shouldBe expectedPoint
    }

    private fun <P : Vector2D<P>> segmentIntersectionShouldBeEmpty(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        intersectionShouldBe<P, Intersection2D.None>(segment1, segment2, false)

    private fun <P : Vector2D<P>> segmentsIntersectionShouldBeInfinite(segment1: Segment2D<P>, segment2: Segment2D<P>) =
        intersectionShouldBe<P, Intersection2D.InfinitePoints>(segment1, segment2, false)

    private fun <P : Vector2D<P>> segmentCircleIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedPoint1: P,
        expectedPoint2: P
    ) {
        val intersection = circleIntersectionShouldBe<P, Intersection2D.MultiplePoints<P>>(
            segment,
            center,
            radius,
            false
        )
        intersection.points shouldContainExactlyInAnyOrder listOf(expectedPoint1, expectedPoint2)
    }

    private fun <P : Vector2D<P>> segmentCircleIntersectionShouldBe(
        segment: Segment2D<P>,
        center: P,
        radius: Double,
        expectedPoint: P
    ) {
        val intersection = circleIntersectionShouldBe<P, Intersection2D.SinglePoint<P>>(
            segment,
            center,
            radius,
            false
        )
        intersection.point shouldBe expectedPoint
    }

    private fun <P : Vector2D<P>> segmentCircleIntersectionShouldBeEmpty(
        segment: Segment2D<P>,
        center: P,
        radius: Double
    ) = circleIntersectionShouldBe<P, Intersection2D.None>(segment, center, radius, false)

    init {
        "test length" {
            horizontalSegment.length shouldBe 4.0
            verticalSegment.length shouldBe 4.0
            obliqueSegment.length shouldBeFuzzy sqrt(2 * 4.0.pow(2))
            degenerateSegment.length shouldBeFuzzy 0.0
        }

        "test isDegenerate" {
            horizontalSegment.isDegenerate shouldBe false
            verticalSegment.isDegenerate shouldBe false
            obliqueSegment.isDegenerate shouldBe false
            degenerateSegment.isDegenerate shouldBe true
        }

        "test isHorizontal" {
            horizontalSegment.isHorizontal shouldBe true
            verticalSegment.isHorizontal shouldBe false
            obliqueSegment.isHorizontal shouldBe false
        }

        "test isVertical" {
            horizontalSegment.isVertical shouldBe false
            verticalSegment.isVertical shouldBe true
            obliqueSegment.isVertical shouldBe false
        }

        "test midPoint" {
            horizontalSegment.midPoint shouldBe coords(4.0, 2.0)
            verticalSegment.midPoint shouldBe coords(2.0, 4.0)
            obliqueSegment.midPoint shouldBe coords(4.0, 4.0)
            degenerateSegment.midPoint shouldBe degenerateSegment.first
        }

        "test toVector" {
            horizontalSegment.toVector shouldBe coords(4.0, 0.0)
            verticalSegment.toVector shouldBe coords(0.0, 4.0)
            obliqueSegment.toVector shouldBe coords(4.0, 4.0)
            degenerateSegment.toVector shouldBe coords(0.0, 0.0)
        }

        "test toLine" {
            horizontalSegment.toLine().let {
                it.slope shouldBeFuzzy 0.0
                it.yIntercept shouldBeFuzzy 2.0
                it.xIntercept shouldBe Double.NaN
            }
            verticalSegment.toLine().let {
                it.slope shouldBe Double.NaN
                it.yIntercept shouldBe Double.NaN
                it.xIntercept shouldBeFuzzy 2.0
            }
            obliqueSegment.toLine().let {
                it.slope shouldBeFuzzy 1.0
                it.yIntercept shouldBeFuzzy 0.0
                it.xIntercept shouldBeFuzzy 0.0
            }
            assertThrows<UnsupportedOperationException> { degenerateSegment.toLine() }
        }

        "test contains" {
            horizontalSegment.contains(coords(2.0, 2.0)) shouldBe true
            horizontalSegment.contains(coords(4.0, 2.0)) shouldBe true
            horizontalSegment.contains(coords(7.0, 2.0)) shouldBe false
            horizontalSegment.contains(coords(4.0, 2.1)) shouldBe false
            verticalSegment.contains(coords(2.0, 2.0)) shouldBe true
            verticalSegment.contains(coords(2.0, 4.0)) shouldBe true
            verticalSegment.contains(coords(2.0, 7.0)) shouldBe false
            verticalSegment.contains(coords(2.1, 4.0)) shouldBe false
            obliqueSegment.contains(coords(2.0, 2.0)) shouldBe true
            obliqueSegment.contains(coords(4.0, 4.0)) shouldBe true
            obliqueSegment.contains(coords(7.0, 7.0)) shouldBe false
            obliqueSegment.contains(coords(4.1, 4.0)) shouldBe false
            degenerateSegment.contains(coords(2.0, 2.0)) shouldBe true
            degenerateSegment.contains(coords(2.1, 2.0)) shouldBe false
        }

        "test closestPointTo and distanceTo point" {
            obliqueSegment.distanceTo(coords(2.0, 0.0)) shouldBe 2.0
            obliqueSegment.distanceTo(obliqueSegment.first) shouldBeFuzzy 0.0
            obliqueSegment.distanceTo(coords(4.0, 2.0)) shouldBeFuzzy sqrt(2.0)
            obliqueSegment.distanceTo(coords(9.0, 9.0)) shouldBeFuzzy 3 * sqrt(2.0)
            val segment = segment(1.0, 3.0, 3.0, 1.0)
            segment.closestPointTo(coords(4.0, 2.0)) shouldBe segment.second
            segment.closestPointTo(coords(4.0, 1.0)) shouldBe segment.second
            segment.closestPointTo(coords(3.0, 2.0)) shouldBe coords(2.5, 1.5)
        }

        "test distanceTo segment" {
            horizontalSegment.distanceTo(horizontalSegment) shouldBeFuzzy 0.0
            horizontalSegment.distanceTo(verticalSegment) shouldBeFuzzy 0.0
            horizontalSegment.distanceTo(obliqueSegment) shouldBeFuzzy 0.0
            horizontalSegment.distanceTo(segment(0.0, 2.0, 2.0, 2.0)) shouldBeFuzzy 0.0
            horizontalSegment.distanceTo(segment(4.0, 0.0, 4.0, 4.0)) shouldBeFuzzy 0.0
            obliqueSegment.distanceTo(segment(0.0, 0.0, 4.0, 0.0)) shouldBeFuzzy 2.0
            obliqueSegment.distanceTo(segment(2.0, 0.0, 8.0, 6.0)) shouldBeFuzzy sqrt(2.0)
        }

        "test isCollinearWith point" {
            horizontalSegment.isCollinearWith(coords(2.0, 2.0)) shouldBe true
            horizontalSegment.isCollinearWith(coords(100.0, 2.0)) shouldBe true
            horizontalSegment.isCollinearWith(coords(2.0, 2.1)) shouldBe false
            verticalSegment.isCollinearWith(coords(2.0, 2.0)) shouldBe true
            verticalSegment.isCollinearWith(coords(2.0, 100.0)) shouldBe true
            verticalSegment.isCollinearWith(coords(2.1, 2.0)) shouldBe false
            obliqueSegment.isCollinearWith(coords(2.0, 2.0)) shouldBe true
            obliqueSegment.isCollinearWith(coords(100.0, 100.0)) shouldBe true
            obliqueSegment.isCollinearWith(coords(2.0, 2.1)) shouldBe false
            degenerateSegment.isCollinearWith(coords(2.0, 2.0)) shouldBe true
            degenerateSegment.isCollinearWith(coords(100.0, 2.0)) shouldBe true
            degenerateSegment.isCollinearWith(coords(2.0, 100.0)) shouldBe true
            degenerateSegment.isCollinearWith(coords(100.0, 100.0)) shouldBe true
        }

        "test isCollinearWith segment" {
            obliqueSegment.isCollinearWith(obliqueSegment) shouldBe true
            obliqueSegment.isCollinearWith(degenerateSegment) shouldBe true
            obliqueSegment.isCollinearWith(segment(8.0, 8.0, 100.0, 100.0)) shouldBe true
            obliqueSegment.isCollinearWith(segment(8.0, 8.0, 8.0, 8.0)) shouldBe true
            obliqueSegment.isCollinearWith(segment(8.0, 8.0, 8.1, 8.0)) shouldBe false
            degenerateSegment.isCollinearWith(obliqueSegment) shouldBe true
            degenerateSegment.isCollinearWith(segment(3.0, 3.0, 8.0, 3.0)) shouldBe false
        }

        "test overlapsWith segment" {
            obliqueSegment.overlapsWith(segment(8.0, 8.0, 100.0, 100.0)) shouldBe false
            obliqueSegment.overlapsWith(segment(0.0, 0.0, 1.0, 1.0)) shouldBe false
            obliqueSegment.overlapsWith(degenerateSegment) shouldBe true
            obliqueSegment.overlapsWith(segment(0.0, 0.0, 100.0, 100.0)) shouldBe true
            obliqueSegment.overlapsWith(segment(3.0, 3.0, 4.0, 4.0)) shouldBe true
            obliqueSegment.overlapsWith(horizontalSegment) shouldBe false
        }

        "test intersect" {
            /*
             * Plain intersection.
             */
            segmentsIntersectionShouldBe(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(3.0, 1.0, 1.0, 3.0),
                coords(2.0, 2.0)
            )
            /*
             * Segments share an endpoint.
             */
            segmentsIntersectionShouldBe(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(5.0, 5.0, 6.0, 1.0),
                coords(5.0, 5.0)
            )
            /*
             * Segments share an endpoint and are collinear.
             */
            segmentsIntersectionShouldBe(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(5.0, 5.0, 6.0, 6.0),
                coords(5.0, 5.0)
            )
            /*
             * Segments are parallel.
             */
            segmentIntersectionShouldBeEmpty(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(1.0, 2.0, 5.0, 6.0)
            )
            /*
             * Segments are not parallel but not intersecting as well.
             */
            segmentIntersectionShouldBeEmpty(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(2.0, 3.0, 1.0, 5.0)
            )
            /*
             * Segments are collinear but disjoint.
             */
            segmentIntersectionShouldBeEmpty(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(6.0, 6.0, 7.0, 7.0)
            )
            /*
             * Segments are coincident.
             */
            segmentsIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(1.0, 1.0, 5.0, 5.0)
            )
            /*
             * Overlapping.
             */
            segmentsIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(3.0, 3.0, 7.0, 7.0)
            )
            /*
             * Overlapping with negative coords.
             */
            segmentsIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(-3.0, -3.0, 4.0, 4.0)
            )
            /*
             * One contains the other.
             */
            segmentsIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(-3.0, -3.0, 7.0, 7.0)
            )
            /*
             * Overlapping and share an endpoint.
             */
            segmentsIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 5.0, 5.0),
                segment(3.0, 3.0, 5.0, 5.0)
            )
            /*
             * Intersections with axis-aligned segments.
             */
            segmentsIntersectionShouldBe(
                segment(1.0, 1.0, 5.0, 1.0),
                segment(3.0, -1.0, 3.0, 1.0),
                coords(3.0, 1.0)
            )
            segmentsIntersectionShouldBe(
                segment(1.0, 1.0, 5.0, 1.0),
                segment(3.0, -1.0, 3.0, 5.0),
                coords(3.0, 1.0)
            )
            segmentsIntersectionShouldBe(
                segment(1.0, 1.0, 5.0, 1.0),
                segment(5.0, 1.0, 6.0, 1.0),
                coords(5.0, 1.0)
            )
            /*
             * Aligned to the x-axis and overlapping.
             */
            segmentsIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 5.0, 1.0),
                segment(4.9, 1.0, 6.0, 1.0)
            )
            /*
             * Aligned to the x-axis and collinear but disjoint.
             */
            segmentIntersectionShouldBeEmpty(
                segment(1.0, 1.0, 5.0, 1.0),
                segment(6.0, 1.0, 7.0, 1.0)
            )
            /*
             * Aligned to the y-axis.
             */
            segmentsIntersectionShouldBe(
                segment(1.0, 1.0, 1.0, 6.0),
                segment(1.0, 1.0, 1.0, -6.0),
                coords(1.0, 1.0)
            )
            segmentIntersectionShouldBeEmpty(
                segment(1.0, 1.0, 1.0, 6.0),
                segment(1.0, -1.0, 1.0, -6.0)
            )
            segmentsIntersectionShouldBeInfinite(
                segment(1.0, 1.0, 1.0, 6.0),
                segment(1.0, 2.0, 1.0, -6.0)
            )
            /*
             * Degenerate segments.
             */
            segmentsIntersectionShouldBe(
                segment(1.0, 1.0, 1.0, 1.0),
                segment(1.0, 1.0, 1.0, 1.0),
                coords(1.0, 1.0)
            )
            segmentIntersectionShouldBeEmpty(
                segment(1.0, 1.0, 1.0, 1.0),
                segment(1.0, 2.0, 1.0, 2.0)
            )
        }

        "test circle intersection" {
            segmentCircleIntersectionShouldBe(
                segment(1.0, 1.0, 5.0, 1.0),
                coords(3.0, 3.0),
                2.0,
                coords(3.0, 1.0)
            )
            segmentCircleIntersectionShouldBeEmpty(
                segment(1.0, -1.0, 5.0, -1.0),
                coords(3.0, 3.0),
                2.0
            )
            segmentCircleIntersectionShouldBe(
                segment(0.0, 3.0, 6.0, 3.0),
                coords(3.0, 3.0),
                2.0,
                coords(1.0, 3.0),
                coords(5.0, 3.0)
            )
            segmentCircleIntersectionShouldBe(
                segment(3.0, 3.0, 6.0, 3.0),
                coords(3.0, 3.0),
                2.0,
                coords(5.0, 3.0)
            )
            segmentCircleIntersectionShouldBeEmpty(
                segment(10.0, 3.0, 12.0, 3.0),
                coords(3.0, 3.0),
                2.0
            )
            segmentCircleIntersectionShouldBe(
                segment(0.0, 1.0, 1.0, 1.0),
                coords(1.0, 1.0),
                1.0,
                coords(0.0, 1.0)
            )
        }
    }
}