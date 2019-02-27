using Microsoft.VisualStudio.TestTools.UnitTesting;
using Mars.Components;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Mars.Components.Tests
{
    [TestClass()]
    public class HelperTests
    {
        [TestMethod()]
        public void ToByteSizeTest()
        {
            long size = 1234567;
            string s = size.ToByteSize();
            
        }
    }
}