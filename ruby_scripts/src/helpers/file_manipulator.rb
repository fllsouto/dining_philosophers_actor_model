module FileManipulator

  RUBY_SCRIPTS_FOLDER = 'ruby_scripts'
  SIMULATION_OUTPUT_DATA = "simulation_output/data"
  SIMULATION_OUTPUT_LOG = "simulation_output/log"
  METRIC_OUTPUT_FOLDER = "metric_input"
  TEMPLATES_FOLDER = "templates"
  PAGES_FOLDER = "pages"

  def root_project_folder
    File.expand_path(File.dirname(__FILE__)).rpartition("/#{RUBY_SCRIPTS_FOLDER}").first
  end

  def simulation_raw_data_in filename
    "#{root_project_folder}/#{filename}"
  end

  def simulation_raw_data_out filename
    "#{root_project_folder}/#{RUBY_SCRIPTS_FOLDER}/#{SIMULATION_OUTPUT_DATA}/#{filename}"
  end

  def simulation_raw_data_folder
    "#{root_project_folder}/#{RUBY_SCRIPTS_FOLDER}/#{SIMULATION_OUTPUT_DATA}"
  end

  def simulation_output_log
    output_filepath = "#{root_project_folder}/#{RUBY_SCRIPTS_FOLDER}/#{SIMULATION_OUTPUT_LOG}"
    output_filename = "Outputtest-#{Time.now.to_i}.txt"
    "#{output_filepath}/#{output_filename}"
  end

  def metric_output_data metric
    "#{root_project_folder}/#{RUBY_SCRIPTS_FOLDER}/#{METRIC_OUTPUT_FOLDER}/#{metric}.json"
  end

  def template_path name
    "#{root_project_folder}/#{RUBY_SCRIPTS_FOLDER}/#{TEMPLATES_FOLDER}/#{name}"
  end

  def template_output name
    "#{root_project_folder}/#{RUBY_SCRIPTS_FOLDER}/#{PAGES_FOLDER}/#{name}"
  end
end