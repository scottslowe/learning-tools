using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Mars.Components
{
    public static class Helper
    {
        private static readonly KeyValuePair<long, string>[] Thresholds =
        {
          // new KeyValuePair<long, string>(0, " Bytes"), // Don't devide by Zero!
            new KeyValuePair<long, string>(1, " Byte"),
            new KeyValuePair<long, string>(2, " Bytes"),
            new KeyValuePair<long, string>(1024, " KB"),
            new KeyValuePair<long, string>(1048576, " MB"), // Note: 1024 ^ 2 = 1026 (xor operator)
            new KeyValuePair<long, string>(1073741824, " GB"),
            new KeyValuePair<long, string>(1099511627776, " TB"),
            new KeyValuePair<long, string>(1125899906842620, " PB"),
            new KeyValuePair<long, string>(1152921504606850000, " EB"),

            // These don't fit into a int64
            // new KeyValuePair<long, string>(1180591620717410000000, " ZB"), 
            // new KeyValuePair<long, string>(1208925819614630000000000, " YB")  
        };

        /// <summary>
        /// Returns x Bytes, kB, Mb, etc... 
        /// </summary>
        public static string ToByteSize(this long value)
        {
            if (value == 0) return "0 Bytes"; // zero is plural
            for (int t = Thresholds.Length - 1; t > 0; t--)
                if (value >= Thresholds[t].Key) return ((double)value / Thresholds[t].Key).ToString("#,##0") + Thresholds[t].Value;
            return "-" + ToByteSize(-value); // negative bytes (common case optimised to the end of this routine)
        }
    }
}
