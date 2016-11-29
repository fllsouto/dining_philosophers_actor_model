# encoding: UTF-8
require 'pry'
require_relative 'data_formatter.rb'
require_relative 'metric_graph.rb'


class DinningMetric

  ROOT_PROJECT_PATH = "/home/fsouto/Study/ime-usp/tcc/dining_actors_https"
  PROJECT_FOLDER = "dinning_simulation"
  METRIC_OUTPUT_FOLDER = "metric_input/"

  def initialize metric
    @metric = metric
    @filename = "#{ROOT_PROJECT_PATH}/#{PROJECT_FOLDER}/#{METRIC_OUTPUT_FOLDER}/#{metric}.json"
  end

  def plot_graph
    formatted_data = DataFormatter.new(@filename, :combo_chart).format_data

    title = formatted_data[:title]
    v_axis = formatted_data[:v_axis]
    h_axis = formatted_data[:h_axis]
    result = formatted_data[:result]

    template_name = "combo_chart.erb"
    output_name = "combo_chart-[#{@metric}]-[#{Time.now}].html"
    metric = MetricGraph.new(title, result, template_name, output_name, v_axis, h_axis).save
  end

end

metrics = ["eating_times", "blocked_times", "waiting_avg"]
metrics.each do |metric|
  dm = DinningMetric.new metric
  dm.plot_graph
end