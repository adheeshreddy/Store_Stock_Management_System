import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import api from "./api/axios";

export default function ViewSupplier() {
  const [suppliers, setSuppliers] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [suppliersPerPage, setSuppliersPerPage] = useState(5);
  const navigate = useNavigate();

  useEffect(() => {
    fetchSuppliers();
  }, [searchTerm, currentPage, suppliersPerPage]);

  const fetchSuppliers = async () => {
    try {
      const response = await api.get("/suppliers", {
        params: { searchKey: searchTerm },
      });

      const allSuppliers = response.data;
      const total = allSuppliers.length;
      setTotalPages(Math.ceil(total / suppliersPerPage));

      const startIndex = (currentPage - 1) * suppliersPerPage;
      const endIndex = startIndex + suppliersPerPage;
      setSuppliers(allSuppliers.slice(startIndex, endIndex));
    } catch (error) {
      alert("Failed to fetch suppliers");
      console.error(error);
    }
  };

  const editSupplier = (supplier) => {
    navigate("/update/supplier", { state: { supplier } });
  };

  const handlePageChange = (page) => {
    if (page < 1 || page > totalPages) return;
    setCurrentPage(page);
  };

  return (
    <>
      <div className="text-center  mt-5 ">
        <h3 className="fw-bold">All Suppliers</h3>
        <div className="d-flex justify-content-center align-items-center">
          <label className="fw-bold mr-2 mt-5">Search:</label>
          <input
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="form-control  mt-5"
            style={{ width: "250px", marginLeft: "10px" }}  
            placeholder="Search suppliers"
          />
        </div>
      </div>

      <div className="d-flex align-items-center justify-content-center">
        <div className="bg-white p-4 rounded shadow" style={{ width: "80%", marginTop: "40px" }}>
          <table className="table table-bordered ">
            <thead>
              <tr>
                <th>Id</th>
                <th>Name</th>
                <th>Email</th>
                <th>Mobile</th>
                <th>Gender</th>
                <th>Country</th>
                <th>State</th>
                <th>City</th>
                <th>Address</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {suppliers.length > 0 ? (
                suppliers.map((supplier) => (
                  <tr key={supplier.id}>
                    <td>{supplier.id}</td>
                    <td>{supplier.name}</td>
                    <td>{supplier.email}</td>
                    <td>{supplier.mobile}</td>
                    <td>{supplier.gender=='M'?"Male":"Female"}</td>
                    <td>{supplier.country}</td>
                    <td>{supplier.state}</td>
                    <td>{supplier.city}</td>
                    <td>{supplier.address}</td>
                    <td>
                      <button
                        className="btn btn-success"
                        onClick={() => editSupplier(supplier)}
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="10" className="text-center">
                    No Suppliers data available.
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          <div className="d-flex justify-content-center align-items-center gap-5">
            {currentPage > 1 && (
              <button
                className="btn btn-primary " 
                onClick={() => handlePageChange(currentPage - 1)}
              >
                Previous
              </button>
            )}

            <span className="mx-3 text-center">
              Page {currentPage} of {totalPages}
            </span>

            {currentPage < totalPages && (
              <button
                className="btn btn-primary"
                onClick={() => handlePageChange(currentPage + 1)}
              >
                Next
              </button>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
