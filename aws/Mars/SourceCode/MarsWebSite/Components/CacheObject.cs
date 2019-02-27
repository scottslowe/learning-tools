using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MarsWebSite.Components
{
    public class CacheObject
    {
        public string Key { get; set; }
        public DateTime Expire { get; set; }
        public object Value { get; set; }
        public CacheObject(string key, DateTime expire, object value) {
            Key = key;
            Expire = expire;
            Value = value;
        }
    }
}
