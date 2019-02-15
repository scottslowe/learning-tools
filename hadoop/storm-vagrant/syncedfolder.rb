# -*- mode: ruby -*-
# vi: set ft=ruby :

class SyncedFolder
  def initialize(name, src, dest, mount_options)
    @name = name
    @src = src
    @dest = dest
    @mount_options = mount_options
  end
  attr_reader :name
  attr_reader :src
  attr_reader :dest
  attr_reader :mount_options
end
