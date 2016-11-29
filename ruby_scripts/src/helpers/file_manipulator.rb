module FileManipulator

  RUBY_SCRIPTS_FOLDER = '/ruby_scrips'
  ROOT_PROJECT_PATH = "/home/fsouto/Study/ime-usp/tcc/dining_actors_https"
  PROJECT_FOLDER = "dinning_simulation"
  SIMULATION_OUTPUT_FOLDER = "simulation_output/data"

  def current_folder
    File.expand_path(File.dirname(__FILE__))
  end

  def root_project_folder
    current_folder.rpartition(RUBY_SCRIPTS_FOLDER).first
  end

  def simulation_raw_log file
    "#{ROOT_PROJECT_PATH}/#{file}"
  end
end