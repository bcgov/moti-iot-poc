using System;
using System.Collections.Generic;
using System.Linq;

namespace oahu_api.Models
{
    public class DeviceRepository : IDeviceRepository
    {
        private readonly PgContext _context;

        public DeviceRepository(PgContext context)
        {
            _context = context;
        }

        public IEnumerable<Device> GetAll()
        {
            return _context.Devices.ToList();
        }

        public void Add(Device device)
        {
            _context.Devices.Add(device);
            _context.SaveChanges();
        }

        public Device Find(long Id)
        {
            return _context.Devices.FirstOrDefault(t => t.id == Id);
        }
    }
}