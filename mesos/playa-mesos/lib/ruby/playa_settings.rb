require 'json'
require 'pathname'
require 'uri'

class PlayaSettings
  attr_reader :settings_file

  def initialize(settings_file)
    @settings_file = settings_file
    @settings = read_user_settings(settings_file)
  end

  def method_missing(sym, *args, &block)
    if @settings.include?(sym.to_s)
      return @settings[sym.to_s]
    else
      super(sym, *args, &block)
    end
  end

  def respond_to?(sym, include_private = false)
    @settings.include?(sym.to_s) || super(sym, include_private)
  end

  # Return the path of the settings_file
  def settings_path
    File.dirname(settings_file)
  end

  # Return the path of the packer builds
  def build_path
    File.join(settings_path, 'packer', 'builds')
  end

  # Aborts (exit with failure code) if URI is invalid
  def box_url
    url = @settings['base_url']
    begin
      URI(url) if url
    rescue URI::Error => e
      abort "Malformed URL in #{settings_file}\n#{e}"
    end
    url
  end

  # Return the last part of the box_url path (the box filename)
  def box_filename
    box_name + '-' + platform + '.box'
  end

  # Return the local filesystem path of the box specified by #box_url if it can
  # be found locally, nil otherwise.
  def box_local
    result = nil
    if box_url
      test_path = File.join(build_path, box_filename)
      result = test_path if File.readable?(test_path)
    end
    result
  end

  private

  def read_user_settings(settings_file)
    remove_nils(JSON.load(File.open(settings_file)))
  rescue
    {}
  end

  def remove_nils(hash)
    hash.delete_if { |k, v| v.nil? }
  end
end
