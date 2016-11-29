module FileManipulator

  RUBY_SCRIPTS_FOLDER = 'ruby_scripts'
  SIMULATION_OUTPUT_DATA = "simulation_output/data"
  SIMULATION_OUTPUT_LOG = "simulation_output/log"

  def root_project_folder
    File.expand_path(File.dirname(__FILE__)).rpartition("/#{RUBY_SCRIPTS_FOLDER}").first
  end

  def simulation_raw_data_in file
    "#{root_project_folder}/#{file}"
  end

  def simulation_raw_data_out file
    "#{root_project_folder}/#{RUBY_SCRIPTS_FOLDER}/#{SIMULATION_OUTPUT_DATA}/#{file}"
  end

  def simulation_output_log
    output_filepath = "#{root_project_folder}/#{RUBY_SCRIPTS_FOLDER}/#{SIMULATION_OUTPUT_LOG}"
    output_filename = "Outputtest-#{Time.now.to_i}.txt"
    "#{output_filepath}/#{output_filename}"
  end
end