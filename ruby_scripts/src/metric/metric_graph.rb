# encoding: UTF-8

require 'erb'
require 'pry'
require_relative '../helpers/file_manipulator'

class MetricGraph
  include ERB::Util
  include FileManipulator

  attr_accessor :title, :data, :template, :v_axis, :h_axis

  def initialize title, data, template_name, output_name, v_axis, h_axis
    @title   = title
    @data  =  data
    @template = get_template(template_name)
    @output = template_output(output_name)
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
    file = File.open(template_path(template_name), "r")
    template = file.read
    file.close
    template
  end
end

