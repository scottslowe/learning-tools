# -*- mode: ruby -*-
# vi: set ft=ruby :

class MachineConfig
  def initialize(type, name, box, memory, cpus, syncs)
    @type = type
    @name = name
    @box = box
    @memory = memory
    @cpus = cpus
    @syncs = syncs
  end
  attr_reader :type
  attr_reader :name
  attr_reader :box
  attr_reader :memory
  attr_reader :cpus
  attr_reader :syncs
end
