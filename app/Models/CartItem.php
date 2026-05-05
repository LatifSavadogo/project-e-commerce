<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class CartItem extends Model
{
    protected $table = 'CartItem';
    protected $primaryKey = 'idcartitem';
    public $timestamps = false;
    protected $guarded = [];
}
