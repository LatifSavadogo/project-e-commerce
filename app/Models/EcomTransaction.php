<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class EcomTransaction extends Model
{
    protected $table = 'EcomTransaction';
    protected $primaryKey = 'idtransaction';
    public $timestamps = false;
    protected $guarded = [];
}
