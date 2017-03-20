using System.Collections.Generic;

namespace oahu_api.Models
{
    public interface IDeviceRepository
    {
        void Add(Device item);
        IEnumerable<Device> GetAll();
        Device Find(long key);
    }
}