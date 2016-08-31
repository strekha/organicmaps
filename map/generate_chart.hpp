#pragma once

#include "indexer/feature_altitude.hpp"

#include "geometry/point2d.hpp"

#include "std/deque.hpp"
#include "std/vector.hpp"

/// \brief fills uniformAltitudeDataM with altitude data which evenly distributed by
/// |resultPointCount| points. |distanceDataM| and |altitudeDataM| form a curve of route altitude.
/// This method is used to generalize and evenly distribute points of the chart.
void NormalizeChartData(deque<double> const & distanceDataM, feature::TAltitudes const & altitudeDataM,
                        size_t resultPointCount, vector<double> & uniformAltitudeDataM);

/// \brief fills |yAxisDataPxl|. |yAxisDataPxl| is formed to pevent displaying
/// big waves on the chart in case of small deviation in absolute values in |yAxisData|.
/// \param height image chart height in pixels.
/// \param minMetersInPixel minimum meter number per height pixel.
/// \param altitudeDataM altitude data vector in meters.
/// \param yAxisDataPxl Y-axis data of altitude chart in pixels.
void GenerateYAxisChartData(size_t height, double minMetersPerPxl,
                            vector<double> const & altitudeDataM, vector<double> & yAxisDataPxl);

/// \brief generates chart image on a canvas with size |width|, |height| with |geometry|.
/// (0, 0) is a left-top corner. X-axis goes down and Y-axis goes right.
/// \param width is result image width in pixels.
/// \param height is result image height in pixels.
/// \param geometry is points which is used to draw a curve of the chart.
/// \param frameBuffer is a vector for a result image. It's resized in this method.
/// It's filled with RGBA(8888) image date.
void GenerateChartByPoints(size_t width, size_t height, vector<m2::PointD> const & geometry,
                          bool day, vector<uint8_t> & frameBuffer);

void GenerateChart(size_t width, size_t height,
                   deque<double> const & distanceDataM, feature::TAltitudes const & altitudeDataM,
                   bool day, vector<uint8_t> & frameBuffer);
