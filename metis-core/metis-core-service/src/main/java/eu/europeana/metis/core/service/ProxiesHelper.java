package eu.europeana.metis.core.service;

import eu.europeana.cloud.common.model.dps.NodeReport;
import eu.europeana.cloud.common.model.dps.NodeStatistics;
import eu.europeana.cloud.common.model.dps.StatisticsReport;
import eu.europeana.metis.core.rest.stats.AttributeStatistics;
import eu.europeana.metis.core.rest.stats.NodePathStatistics;
import eu.europeana.metis.core.rest.stats.NodeValueStatistics;
import eu.europeana.metis.core.rest.stats.RecordStatistics;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

class ProxiesHelper {

  ProxiesHelper() {
  }

  RecordStatistics convertRecordStatistics(StatisticsReport report) {

    // Group the node statistics by their respective xpath.
    final Map<String, List<NodeStatistics>> nodesByXPath = report.getNodeStatistics().stream()
        .collect(Collectors.groupingBy(NodeStatistics::getXpath));
    final List<NodePathStatistics> nodePathStatisticsList = nodesByXPath.entrySet().stream()
        .map(ProxiesHelper::convertNodePathStatistics)
        .sorted(Comparator.comparing(NodePathStatistics::getxPath))
        .collect(Collectors.toList());

    // Done.
    final RecordStatistics result = new RecordStatistics();
    result.setNodePathStatistics(nodePathStatisticsList);
    result.setTaskId(report.getTaskId());
    return result;
  }

  private static NodePathStatistics convertNodePathStatistics(
      Entry<String, List<NodeStatistics>> nodeWithXPath) {
    return convertNodePathStatistics(nodeWithXPath.getKey(), nodeWithXPath.getValue(),
        ProxiesHelper::convertNodeValueStatistics);
  }

  NodePathStatistics convertNodePathStatistics(String nodePath, List<NodeReport> nodeReports) {
    return convertNodePathStatistics(nodePath, nodeReports,
        ProxiesHelper::convertNodeValueStatistics);
  }

  private static <I> NodePathStatistics convertNodePathStatistics(String nodePath,
      List<I> nodes, Function<I, NodeValueStatistics> nodeValueConverter) {
    final List<NodeValueStatistics> nodeValueStatisticsList = nodes.stream()
        .map(nodeValueConverter)
        .sorted(Comparator.comparing(NodeValueStatistics::getValue))
        .collect(Collectors.toList());
    final NodePathStatistics nodePathStatistics = new NodePathStatistics();
    nodePathStatistics.setxPath(nodePath);
    nodePathStatistics.setNodeValueStatistics(nodeValueStatisticsList);
    return nodePathStatistics;
  }

  private static NodeValueStatistics convertNodeValueStatistics(NodeStatistics nodeStatistics) {
    return convertNodeValueStatistics(nodeStatistics.getValue(), nodeStatistics.getOccurrence(),
        nodeStatistics.getAttributesStatistics());
  }

  private static NodeValueStatistics convertNodeValueStatistics(NodeReport nodeReport) {
    return convertNodeValueStatistics(nodeReport.getNodeValue(), nodeReport.getOccurrence(),
        nodeReport.getAttributeStatistics());
  }

  private static NodeValueStatistics convertNodeValueStatistics(String nodeValue,
      long occurrence,
      Collection<eu.europeana.cloud.common.model.dps.AttributeStatistics> attributes) {
    final List<AttributeStatistics> attributeStatistics = attributes.stream()
        .map(ProxiesHelper::convertAttributeStatistics)
        .sorted(Comparator.comparing(AttributeStatistics::getxPath)
            .thenComparing(AttributeStatistics::getValue))
        .collect(Collectors.toList());
    final NodeValueStatistics nodeValueStatistics = new NodeValueStatistics();
    nodeValueStatistics.setValue(nodeValue);
    nodeValueStatistics.setOccurrences(occurrence);
    nodeValueStatistics.setAttributeStatistics(attributeStatistics);
    return nodeValueStatistics;
  }

  private static AttributeStatistics convertAttributeStatistics(
      eu.europeana.cloud.common.model.dps.AttributeStatistics input) {
    final AttributeStatistics attributeStatistics = new AttributeStatistics();
    attributeStatistics.setxPath(input.getName());
    attributeStatistics.setValue(input.getValue());
    attributeStatistics.setOccurrence(input.getOccurrence());
    return attributeStatistics;
  }
}
