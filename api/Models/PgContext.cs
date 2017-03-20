using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace oahu_api.Models
{
    public class PgContext : DbContext
    {
        public PgContext(DbContextOptions<PgContext> options)
            : base(options)
        {
        }

        public DbSet<Device> Devices { get; set; }        
        public DbSet<Event> Events { get; set; }        
    }
}