# encoding: UTF-8
#TODO change name!!!
require 'multi_json'
require 'pry'

class Philolog
  attr_reader :name

  def initialize name, data
    @name, sep, @position = name.rpartition("--")
    @thinking_delay, @eating_delay = get_random_times data
    @dinner_log = clean_data(data, [:actor, :philo])
  end

  def philo_eating_time
    messages = query_messages(@dinner_log, ["FellHungry", "StartEating"])
    states = query_states(messages, ["thinking", "eating"])
    sorted_states = sort_data(states)
    return calculate_philo_time(sorted_states)
  end

  def blocked_times
    # query_messages(@dinner_log, ["BlockedToTake"]).count
    result = query_states(@dinner_log, ["thinking"])
    query_messages(result, ["AlreadyHungry"]).count
  end

  def eating_times
    query_messages(@dinner_log, ["StartEating"]).size
  end

  def position
    @position
  end

  def waiting_avg
    eating_time = philo_eating_time
    return 0 if eating_time.empty?

    size = eating_time.size

    sum_time/(size*1.0)
  end

  def to_s
    puts ">"*50
    puts "Name: #{@name}"
    puts "Position: #{@position}"
    puts "Messages: #{@dinner_log.size}"
    puts "Delay (Think, Eat): (#{@thinking_delay}, #{@eating_delay})"
    puts "Eating times : #{self.philo_eating_time.size}"
    puts "Blocked times : #{blocked_times}"
    # puts "Eating waiting : #{self.philo_eating_time}"
    printf("Sum waiting eating time : %.4f\n", sum_time)
    printf("Average waiting eating time : %.4f\n", waiting_avg)

    describe_states
    puts "<"*50

    # puts "\n"
  end

  private
  def get_random_times data
    receive = data.select {|i| i[:state] == "receive"}.first
    return [receive[:tTime], receive[:eTime]]
  end

  def describe_states
    data_hash = @dinner_log.dup
    states = data_hash.group_by { |d| d[:state]}
    
    puts "\n::States and Messages::\n\n"
    states.each do |state, msgs|
      puts "State: #{state}"
      msgs.group_by { |d| d[:message]}.each do |msg, dmsg|
        puts "\tMessage: #{msg} -- #{dmsg.size}"
      end
    end
  end

  def query_messages data, messages_type
    data.select{ |d| messages_type.include?(d[:message]) }
  end

  def query_states data, states_type
    data.select{ |d| states_type.include?(d[:state]) }
  end

  def sort_data data
    data.sort{|a,b| a[:timestamp] <=> b[:timestamp]}
  end

  def sum_time
    eating_time = philo_eating_time
    return 0 if eating_time.empty?

    eating_time.reduce(:+)
  end

  def calculate_philo_time data
    start_time = data.first[:timestamp] #TODO: see this
    size = pop_odd(data).size
    times = []
    (0...size).step(2).each do |i|
      times << ((data[i+1][:timestamp] - data[i][:timestamp])/1000.0)
    end
    times
  end

  def pop_odd data
    return data if (data.size % 2) == 0
    data.pop
    return data
  end
  
  def group_states data
    custom_group_by(:state, data)
  end

  def clean_data data, key
    key.each do |k|
      data.map do |d|
        d.delete(k)
      end
    end
    data
  end

end

class DinningLogParser
  attr_accessor :philos, :data_hash
  def initialize filename
    @filename = filename
    @algorithm = filename.
      rpartition("simulation_output/data/").
      last.
      partition("-").
      first.
      gsub("DinnerMaster", "")
  end

  def parse_file
    @data_hash = open_and_convert_to_json

    data = sort_data data_hash

    @philos = parse_philo_data(data_hash)
  end

  def eating_times
    eating_times_hash = {}

    eating_times_hash = philos.each_with_object({}) do |philo, hs|
      hs[philo.name] = philo.eating_times
    end

    return @algorithm, eating_times_hash
  end

  def blocked_times
    blocked_times_hash = {}

    blocked_times_hash = philos.each_with_object({}) do |philo, hs|
      hs[philo.name] = philo.blocked_times
    end

    return @algorithm, blocked_times_hash
  end

  def waiting_avg
    waiting_avg_hash = {}

    waiting_avg_hash = philos.each_with_object({}) do |philo, hs|
      hs[philo.name] = philo.waiting_avg
    end

    return @algorithm, waiting_avg_hash
  end

private

  def open_and_convert_to_json
    file = File.read(@filename).chomp(',')
    file = "[#{file}]"
    MultiJson.load(file, :symbolize_keys => true)
  end

  def sort_data data
    data.sort{|a,b| a[:timestamp] <=> b[:timestamp]}
  end

  def parse_philo_data data_hash
    philo_array = (create_philo_array data_hash).sort{|a,b| a.position <=> b.position}
    # philo_array.each { |p| puts "#{p.to_s}"} # <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
  end

  def create_philo_array raw_data
    philo_raw_data    = actor_filter(raw_data, "Philosopher")
    philo_names       = get_philos_name philo_raw_data
    philo_group_data  = group_philos(philo_raw_data)
    return philo_names.map do |name|
      Philolog.new(name, philo_group_data[name])
    end
  end

  def actor_filter data, actor_type
    data.select { |d| d[:actor] == actor_type }
  end

  def get_philos_name data
    data.select {|i| i[:state] == "receive"}.map{|p| p[:philo]}
  end

  def group_philos data
    data.group_by {|d| d[:philo] }
  end
end

class DinningLog

  ROOT_PROJECT_PATH = "/home/fsouto/Study/ime-usp/tcc/dining_actors_https"
  PROJECT_FOLDER = "dinning_simulation"
  METRIC_OUTPUT_FOLDER = "metric_input/"

  attr_accessor :dlps
  def initialize filenames
    @filenames = filenames
    @dlps = []
    @filenames.each { |filename| @dlps << DinningLogParser.new(filename) }
  end

  def parse_files
    @dlps.map { |dlp| dlp.parse_file }
  end


  def write_metric_input metric
    case metric
    when :eating_times
      filename = "#{ROOT_PROJECT_PATH}/#{PROJECT_FOLDER}/#{METRIC_OUTPUT_FOLDER}/#{metric}.json"
      save_data_to_file(filename, eating_times.to_json)
    when :blocked_times
      filename = "#{ROOT_PROJECT_PATH}/#{PROJECT_FOLDER}/#{METRIC_OUTPUT_FOLDER}/#{metric}.json"
      save_data_to_file(filename, blocked_times.to_json)
    when :waiting_avg
      filename = "#{ROOT_PROJECT_PATH}/#{PROJECT_FOLDER}/#{METRIC_OUTPUT_FOLDER}/#{metric}.json"
      save_data_to_file(filename, waiting_avg.to_json)
    end

      
  end

private

  def eating_times
    eating_times = @dlps.each_with_object({}) do |dlp, hs|
      alg, e_times = dlp.eating_times
      hs[alg] = e_times
    end
    {EatingTimes: eating_times}
  end

  def blocked_times
    blocked_times = @dlps.each_with_object({}) do |dlp, hs|
      alg, e_times = dlp.blocked_times
      hs[alg] = e_times
    end
    {BlockedTimes: blocked_times}
  end

  def waiting_avg
    waiting_avg = @dlps.each_with_object({}) do |dlp, hs|
      alg, e_times = dlp.waiting_avg
      hs[alg] = e_times
    end
    {WaitingAverage: waiting_avg}
  end

  def save_data_to_file filename, data
    open(filename, 'w') { |f| f.puts data }
  end

end

FILEPATH = "/home/fsouto/Study/ime-usp/tcc/dining_actors_https/dinning_simulation/simulation_output/data"

filenames = `ls -A1 #{FILEPATH} | grep .json`.split("\n").map {|f| "#{FILEPATH}/#{f}"}

metrics = [:eating_times, :blocked_times, :waiting_avg]
dls = []
metrics.each do |metric|
  dl = DinningLog.new(filenames)
  dl.parse_files
  dl.write_metric_input metric
  dls << dl
end
