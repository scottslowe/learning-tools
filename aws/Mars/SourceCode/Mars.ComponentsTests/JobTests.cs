using Mars.Components;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Mars.ComponentsTests
{
    [TestClass()]
    public class JobTests
    {
        [TestMethod()]
        public void GetJobsTest()
        {
            string keywords = "angular";
            string location = "toronto";
            var jobList = new JobService().GetJobs(keywords, location);

            Assert.IsTrue(jobList[0].Title.ToLower().Contains(keywords));
            Assert.IsTrue(jobList[0].Location.ToLower().Contains(location));
        }
    }
}
