

% Extend 2D image boundaries by mirroring
%   y_ext: extension along the rows
%   x_ext: extension along the columns      
% Function: bound_extension
% 
% Extend 2D image boundaries by mirroring
% 
% Usage:
%   : function [img_ext : mat] = bound_extension2d(img : mat, y_ext : int, x_ext : int)
% 
% Parameters:
% img - input image
% y_ext - extension along the rows
% x_ext - extension along the columns
% 
% Returns:
% img_ext - extended image
function [img_ext:mat] = bound_extension2d(img:mat, y_ext:int, x_ext:int)
    img_ext = bound_extension3d(reshape(img, [1, size(img, 0), size(img, 1)]), 0, y_ext, x_ext)
    img_ext = reshape(img_ext, size(img_ext, 1..2))
end

% Extend 3D image boundaries by mirroring
%   y_ext: extension along the rows
%   x_ext: extension along the columns      
% Function: bound_extension
% 
% Extend 3D image boundaries by mirroring
% 
% Usage:
%   : function [img_ext : mat] = bound_extension3d(img : mat, y_ext : int, x_ext : int)
% 
% Parameters:
% img - input image
% y_ext - extension along the rows
% x_ext - extension along the columns
% 
% Returns:
% img_ext - extended image
function [img_ext:cube] = bound_extension3d(img:cube, z_ext:int, y_ext:int, x_ext:int)
    [Nz,Ny,Nx] = size(img)
    img_ext = zeros(Nz+2*z_ext,Ny+2*y_ext,Nx+2*x_ext)
    
    img_ext[z_ext..Nz+z_ext-1,y_ext..Ny+y_ext-1,x_ext..Nx+x_ext-1] = img
    
    img_ext[0..z_ext-1,:,:] = img_ext[2*z_ext-1..-1..z_ext,:,:]
    img_ext[:,0..y_ext-1,:] = img_ext[:,2*y_ext-1..-1..y_ext,:]
    img_ext[:,:,0..x_ext-1] = img_ext[:,:,2*x_ext-1..-1..x_ext]
    
    img_ext[Nz+z_ext..Nz+2*z_ext-1,:,:] = img_ext[Nz+z_ext-1..-1..Nz,:,:]
    img_ext[:,Ny+y_ext..Ny+2*y_ext-1,:] = img_ext[:,Ny+y_ext-1..-1..Ny,:]
    img_ext[:,:,Nx+x_ext..Nx+2*x_ext-1] = img_ext[:,:,Nx+x_ext-1..-1..Nx]
    
    img_ext[0..z_ext-1, 0..y_ext-1, 0..x_ext-1] = img_ext[2*z_ext-1..-1..z_ext, 2*y_ext-1..-1..y_ext, 2*x_ext-1..-1..x_ext]
    img_ext[Nz+z_ext..Nz+2*z_ext-1, 0..y_ext-1, 0..x_ext-1] = img_ext[Nz+z_ext-1..-1..Nz, 2*y_ext-1..-1..y_ext, 2*x_ext-1..-1..x_ext]
    img_ext[0..z_ext-1, Ny+y_ext..Ny+2*y_ext-1, 0..x_ext-1] = img_ext[2*z_ext-1..-1..z_ext, Ny+y_ext-1..-1..Ny, 2*x_ext-1..-1..x_ext]
    img_ext[0..z_ext-1, 0..y_ext-1, Nx+x_ext..Nx+2*x_ext-1] = img_ext[2*z_ext-1..-1..z_ext, 2*y_ext-1..-1..y_ext, Nx+x_ext-1..-1..Nx]
    img_ext[Nz+z_ext..Nz+2*z_ext-1, Ny+y_ext..Ny+2*y_ext-1, 0..x_ext-1] = img_ext[Nz+z_ext-1..-1..Nz, Ny+y_ext-1..-1..Ny, 2*x_ext-1..-1..x_ext]
    img_ext[Nz+z_ext..Nz+2*z_ext-1, 0..y_ext-1, Nx+x_ext..Nx+2*x_ext-1] = img_ext[Nz+z_ext-1..-1..Nz, 2*y_ext-1..-1..y_ext, Nx+x_ext-1..-1..Nx]
    img_ext[0..z_ext-1, Ny+y_ext..Ny+2*y_ext-1, Nx+x_ext..Nx+2*x_ext-1] = img_ext[2*z_ext-1..-1..z_ext, Ny+y_ext-1..-1..Ny, Nx+x_ext-1..-1..Nx]
    img_ext[Nz+z_ext..Nz+2*z_ext-1, Ny+y_ext..Ny+2*y_ext-1, Nx+x_ext..Nx+2*x_ext-1] = img_ext[Nz+z_ext-1..-1..Nz, Ny+y_ext-1..-1..Ny, Nx+x_ext-1..-1..Nx]
    
end