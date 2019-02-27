using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MarsWebSite.Components
{
    public class SimpleCache
    {
        private Dictionary<string, CacheObject> _simpleCache;
        public SimpleCache() {
            _simpleCache = new Dictionary<string, CacheObject>();
        }
        public void Add(string key, int time, object value) {
            _simpleCache[key] = new CacheObject(key, DateTime.Now.AddMinutes(time), value);
        }

        public object Get(string key){
            object value = null;
            if ( _simpleCache.ContainsKey(key) 
                && _simpleCache[key] != null 
                && _simpleCache[key].Expire > DateTime.Now ) {
                value = _simpleCache[key].Value;
            }

            return value;
        }
    }
}
