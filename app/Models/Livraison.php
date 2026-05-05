<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Livraison extends Model
{
    protected $table = 'Livraison';
    protected $primaryKey = 'idlivraison';
    public $timestamps = false;
    protected $guarded = [];
}
