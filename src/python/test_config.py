
import ConfigParser

config = ConfigParser.ConfigParser()
config.read("my.config")

print dir(config)
print config._sections
print config.get('core','log')
