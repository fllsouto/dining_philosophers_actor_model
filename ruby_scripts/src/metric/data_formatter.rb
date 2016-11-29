# encoding: UTF-8
require 'multi_json'
require 'pry'

class DataFormatter

  def initialize filename, graph_type
    @data = open_and_convert_to_json(filename)
    @graph_type = graph_type
  end

  def format_data
    case @graph_type
      when :combo_chart
        return format_combo_chart_data
      when :timeline
        return format_timeline_data
    end 
  end

private

  def format_combo_chart_data
    data = @data.dup
    formatted_data_metrics = {}
    current_metric = nil
    data.each do |metric, metric_results|
      current_metric = metric
      formatted_metric = [["Algorithms"]]
      metric_results.each do |algorithm, philosophers|
        if(formatted_metric[0].size == 1)
          formatted_metric[0] += philosophers.keys.map{ |k| k.to_s} + ["Média"]
        end
        alg_times = []
        alg_times <<  set_algorithm(algorithm.to_s)   
        philosophers.each { |philosopher, time| alg_times << time }
        alg_times <<  alg_times.drop(1).instance_eval { reduce(:+)/size.to_f }   
        formatted_metric.push(alg_times)
      end
      formatted_data_metrics[metric] = { result: formatted_metric }.merge(get_additional_info(metric))
    end
    formatted_data_metrics[current_metric]
  end

  def set_algorithm alg
    case alg
    when "ChandyMisra"
      return "Chandy-Misra"
    when "ResourceHierarchy"
      return "Hierarquia de Recursos"
    when "Waiter"
      return "Waiter"
    end
  end

  def get_additional_info metric
    case metric
      when :EatingTimes
        return { 
          title: "Média de vezes que um filósofo comeu",
          v_axis: "# Comeu",
          h_axis: "Algoritmo"
        }
      when :BlockedTimes
        return { 
          title: "Média de vezes que um filósofo foi bloqueado",
          v_axis: "# Bloqueado",
          h_axis: "Algoritmo"
        }
      when :WaitingAverage
        return { 
          title: "Tempo médio de esperar para comer",
          v_axis: "Tempo (s)",
          h_axis: "Algoritmo"
        }
      end
  end
  
  def format_timeline_data
   [['Month', 'waka', 'foo', 'bar', 'Papua nova wololo', 'Rwanda', 'Average'],
   ['2004/05',  165,      938,         522,             998,           450,      614.6],
   ['2005/06',  135,      1120,        599,             1268,          288,      682],
   ['2006/07',  157,      1167,        587,             807,           397,      623],
   ['2007/08',  139,      1110,        615,             968,           215,      609.4],
   ['2008/09',  136,      691,         629,             1026,          366,      569.6]]
  end

  def open_and_convert_to_json filename
    file = File.open(filename, 'r')
    data = MultiJson.load(file.read, :symbolize_keys => true)
    file.close
    data
  end
end