# encoding: UTF-8
#http://stackoverflow.com/questions/980547/how-do-i-execute-ruby-template-files-erb-without-a-web-server-from-command-lin
#http://www.stuartellis.name/articles/erb/
#http://ruby-doc.org/stdlib-2.3.1/libdoc/erb/rdoc/ERB.html
#https://developers.google.com/chart/interactive/docs/printing

require 'erb'
require 'pry'

class MetricGraph
  include ERB::Util
  attr_accessor :title, :data, :template, :v_axis, :h_axis

  def initialize title, data, template_name, output_name, v_axis, h_axis
    @title   = title
    @data  =  data
    @template = get_template(template_name)
    @output = get_output(output_name)
    @v_axis = v_axis
    @h_axis = h_axis
  end

  def render
    ERB.new(@template).result(binding)
  end

  def save
    File.open(@output, "w+") do |f|
      f.write(render)
    end
  end

private

  def get_template template_name
    fullpath_filename = "/home/fsouto/Study/ime-usp/tcc/dining_actors_https/dinning_simulation/templates/#{template_name}"
    file = File.open(fullpath_filename, "r")
    template = file.read
    file.close
    template
  end

  def get_output output_name
    "/home/fsouto/Study/ime-usp/tcc/dining_actors_https/dinning_simulation/pages/#{output_name}"
  end
end

