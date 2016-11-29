# encoding: UTF-8
require 'pry'
require 'benchmark'

class DinnerAutomaticExecuter

  def initialize debug=false
    @debug = debug
    @algorithms = {
      # waiter: "W",
      # resource: "R"
      chandymisra: "C"
    }

    @options = {
      yes: "Y"
      # no:  "N"
    }

    @factor_options = {
      exp3: {key: "Exponential3", min:1, max: 5},
      exp4: {key: "Exponential4", min:1, max: 5},
      exp5: {key: "Exponential5", min:1, max: 5},
      hdr: {key: "Hundred", min:1, max: 5},
      fixed: {key: "Fixed", min:0, max: 1},
    }
    output_filepath = "/home/fsouto/Study/ime-usp/tcc/dining_actors_https/dinning_simulation/simulation_output/log"
    output_filename = "Outputtest-#{Time.now.to_i}.txt"
    @filename = "#{output_filepath}/#{output_filename}"
  end

  def run_simulation option=:fixed
    simulation_times = calculate_simulations_time option

    execution_results = {}
    @algorithms.each do |k_alg, alg|
      @options.each do |k_option, option|
        simulation_times.each do |k_sim, simulation_time|
          show_exec_command(k_alg, option, simulation_time) if @debug
          result = exec_command(alg, option, simulation_time)
          key = "#{k_alg}_#{k_option}_#{k_sim}".to_sym
          execution_results[key] = result
        end
      end  
    end
    
    execution_results
  end


  private

  def calculate_simulations_time option
    simulation_times = {}
    for i in @factor_options[option][:min]..@factor_options[option][:max]
      key = "st#{i}".to_sym
      simulation_times[key] = calculate_st i, @factor_options[option][:key]
    end
    simulation_times
  end

  def calculate_st factor, factor_option
    case factor_option
    when "Exponential3"
      return (3**factor)*60
    when "Exponential4"
      return (4**factor)*60
    when "Exponential5"
      return (5**factor)*60
    when "Hundred"
      return (100*factor)*60
    when "Fixed"
      # fixed_time = [5, 10, 25, 50, 100, 250]
      fixed_time = [1,2]
      return fixed_time[factor] * 60
    end
  end

  def fact_to_min f
    f/60.0
  end

  def exec_command alg, option, sim_t
    puts "Starting simulation at: #{Time.now}"
    cmd = "sbt \"run-main br.usp.ime.fllsouto.dinningActors.DinningPhilosophers #{sim_t} #{alg} #{option}\""
    puts "Command : #{cmd}"
    result = nil
    time = Benchmark.measure {
      result = `#{cmd}` #TODO : save output
    }
    puts "#{time}" if @debug
    puts "#{result}" if @debug
    puts "Ending simulation at: #{Time.now}\n\n"
    save_data_to_file time, result    
    result
  end

  def show_exec_command alg, option, sim_t
    puts ">" * 60
    puts "Algorithm: #{alg}"
    puts "Option: #{option}"
    puts "Simulation time: #{sim_t} s, #{fact_to_min(sim_t)} m"
    puts "<" * 60
    puts "\n\n"
  end

  def save_data_to_file time, result
    open(@filename, 'a') { |f|
      f.puts time
      f.puts result
      f.puts "\n\n"
    }
  end

end

class LogFileOperator
  
  ROOT_PROJECT_PATH = "/home/fsouto/Study/ime-usp/tcc/dining_actors_https"
  PROJECT_FOLDER = "dinning_simulation"
  SIMULATION_OUTPUT_FOLDER = "simulation_output/data"

  def initialize simulations_output
    @simulations = simulations_output
  end

  attr_reader :outputs
  def get_output_names
    @outputs = []
    term = "output file saved in :"
    @simulations.each do |simulation_name, result|
      lines = result.split("\n")
      line = lines.select { |line| line.index(term) != nil}.first
      head, sep, filename = line.rpartition term
      @outputs << filename.gsub(' ', '')
    end
    @outputs
  end

  def move_files
    puts "\n"
    @outputs.each do |file|
      puts "Moving file #{file} ..."
      src = simulation_raw_log(file)
      dest = "#{ROOT_PROJECT_PATH}/#{PROJECT_FOLDER}/#{SIMULATION_OUTPUT_FOLDER}/#{file}"
      FileUtils.mv(src, dest)
    end
    puts "All files moved!"
  end
end


debug_flag =  (!ARGV[0].nil? && ARGV[0] == "--debug") ? true : false

dae = DinnerAutomaticExecuter.new(debug_flag)
results = dae.run_simulation
lfo = LogFileOperator.new(results)
lfo.get_output_names
lfo.move_files